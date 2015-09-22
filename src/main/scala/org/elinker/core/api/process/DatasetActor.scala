package org.elinker.core.api.process

import java.io.ByteArrayInputStream
import java.net.URL
import java.util

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy}
import akka.event.Logging
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.rdf.model.{Resource, Literal, Model, ModelFactory}
import com.hp.hpl.jena.shared.{JenaException, SyntaxError}
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.elinker.core.api.db.DB
import org.elinker.core.api.db.Tables._
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing.RequestContext

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Created by nilesh on 16/12/2014.
 */
object DatasetActor {
  case class CreateDataset(name: String, description: String, format: String, data: InputType, defaultLang: String, properties: Seq[String])
  case class UpdateDataset(name: String, description: String, format: String, data: InputType, defaultLang: String, properties: Seq[String])
  case class DeleteDataset(name: String)
  case class ShowDataset(name: String)
  case class GetDataset(name: String)
  case class ListDatasets()

  abstract class InputType()
  case class TextInput(text: String) extends InputType
  case class UrlInput(url: String) extends InputType
  case class SparqlInput(query: String, endpoint: String) extends InputType
}

object JsonImplicits extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DatasetsJsonFormat extends RootJsonFormat[datasets] with CollectionFormats {

    import spray.json._

    def write(a: datasets) = a match {
      case datasets(name, description, totalentities, creationtime) => JsObject(
        "Name" -> JsString(name.get),
        "Description" -> JsString(description.get),
        "TotalEntities" -> JsNumber(totalentities.get),
        "CreationTime" -> JsNumber(creationtime.get.toInstant.toEpochMilli)
      )
    }

    def read(value: JsValue) = ???
  }

  implicit object MapJsonFormat extends RootJsonFormat[Map[String, Any]] {
    def write(m: Map[String, Any]) = {
      JsObject(m.mapValues {
        case v: String => JsString(v)
        case v: Int => JsNumber(v)
        case v: Map[String, Any] => write(v)
        case v: Any => JsString(v.toString)
      })
    }

    def read(value: JsValue) = ???
  }

}

class DatasetActor(rc: RequestContext) extends Actor with DB {

  import DatasetActor._
  import JsonImplicits._
  import context._

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient("http://localhost:8983/solr")

  val defaultIndexProps = Seq("http://www.w3.org/2004/02/skos/core#prefLabel",
    "http://www.w3.org/2004/02/skos/core#altLabel",
    "http://www.w3.org/2000/01/rdf-schema#label")

  def indexData(dataset: String, format: String, body: InputType, defaultLang: String, properties: Seq[String]): Long = {
    def addLabel(resource: Resource, label: Literal) = {
      val uri = resource.getURI
      val document = new SolrInputDocument()
      val language = label.getLanguage
      val partialUpdate = new util.HashMap[String, String]()
      partialUpdate.put("add", label.getString)
      document.addField("label", partialUpdate)
      document.addField("dataset", dataset)
      document.addField("resource", uri)
      document.addField("language", if (language == "") defaultLang else language)
      document.addField("count", 1)
      solr.add("elinker", document)
    }

    def indexModel(model: Model) = {
      for (property <- properties) {
        for ((resource, label) <- iterateEntityValues(model, property)) {
          addLabel(resource, label)
        }
      }
    }

    body match {
      case TextInput(text) =>
        // RDF data sent in POST body
        val model = ModelFactory.createDefaultModel()
        model.read(new ByteArrayInputStream(text.getBytes), null, format)
        indexModel(model)
      case UrlInput(url) =>
        // Fetch RDF data from given URL
        val model = ModelFactory.createDefaultModel()
        model.read(new URL(url).openStream(), null, format)
        indexModel(model)
      case SparqlInput(query, endpoint) =>
        // Fetch RDF from SPARQL endpoint
        val qe = QueryExecutionFactory.sparqlService(endpoint, query)
        val results = qe.execSelect()
        for(result <- results) {
          val resource = result.get("res").asResource()
          val label = result.get("label").asLiteral()
          addLabel(resource, label)
        }
    }

    solr.commit("elinker", true, true)
    val sizeQuery = new SolrQuery()
    sizeQuery.set("q", s"dataset:$dataset")
    sizeQuery.set("rows", "0")
    val result = solr.query("elinker", sizeQuery)
    result.getResults.getNumFound
  }

  def iterateEntityValues(model: Model, property: String): Traversable[(Resource, Literal)] = {
    val iter = model.listStatements(null, model.getProperty(property), null)
    new Traversable[(Resource, Literal)] {
      override def foreach[U](f: ((Resource, Literal)) => U): Unit = {
        for (statement <- iter) {
          f((statement.getSubject.asResource(), statement.getObject.asLiteral()))
        }
      }
    }
  }

  def createDataset(dataset: CreateDataset): (Long, Long) = {
    val numEntities = indexData(dataset.name, dataset.format, dataset.data, dataset.defaultLang,
      if (dataset.properties.nonEmpty) dataset.properties else defaultIndexProps)
    val timeStamp = System.currentTimeMillis()
    database withSession {
      implicit session =>
        sqlu"""
                 INSERT OR REPLACE INTO Datasets VALUES (${dataset.name}, ${dataset.description}, $numEntities, $timeStamp)
              """.first
    }
    (numEntities, timeStamp)
  }

  def deleteDataset(name: String) = {
    solr.deleteByQuery("elinker", s"dataset:$name")
    solr.commit("elinker")
    database withSession {
      implicit session =>
        sqlu"""
                 DELETE FROM Datasets WHERE name = $name
              """.first
    }
  }

  def getDataset(name: String): List[datasets] = {
    database withSession {
      implicit session =>
        val query = Q.query[String, datasets]("SELECT * FROM Datasets WHERE name = ?")
        query(name).list
    }
  }

  def receive = {
    case message @ CreateDataset(name, description, _, _, _, _) =>
      // Check whether dataset already exists before attempting to create a new one.
      val datasets = getDataset(name)
      if (datasets.isEmpty) {
        try {
          val (numEntities, timeStamp) = createDataset(message)
          complete(Created, Map(
            "Status" -> "Dataset created successfully.",
            "DatasetInfo" -> Map(
              "Name" -> name,
              "Description" -> description,
              "TotalEntities" -> numEntities,
              "CreationTime" -> timeStamp
            )
          ).asInstanceOf[Map[String, Any]])
        } catch {
          case ex: SyntaxError =>
            complete(BadRequest, Map("Status" -> "Syntax error found in RDF data!",
              "Error" -> ex.toString))
          case ex: JenaException =>
            complete(BadRequest, Map("Status" -> "Exception occurred while creating Jena model!",
              "Error" -> ex.toString))
        }
      } else {
        complete(Conflict, Map("Status" -> s"""Dataset with name "$name" already exists."""))
      }

    case UpdateDataset(name, description, format, body, defaultLang, properties) =>
      // NOTE: Updating a dataset adds new labels and does not remove anything. This is technically not equivalent to a
      // PUT-based update but we use this for PUT because it's more convenient for incrementally adding a dataset.
      val (numEntities, timeStamp) = createDataset(CreateDataset(name, description, format, body, defaultLang, properties))
      complete(OK, Map(
        "Status" -> "Dataset updated successfully.",
        "DatasetInfo" -> Map(
          "Name" -> name,
          "Description" -> description,
          "TotalEntities" -> numEntities,
          "CreationTime" -> timeStamp
        )
      ).asInstanceOf[Map[String, Any]])

    case DeleteDataset(name) =>
      val datasets = getDataset(name)

      if (datasets.nonEmpty) {
        deleteDataset(name)
        complete(OK, Map("Status" -> s"Dataset successfully deleted."))
      } else {
        complete(NotFound, Map("Status" -> s"""Dataset with name "$name" does not exist."""))
      }

    case GetDataset(name) =>
      // Convenience message to fetch metadata about a dataset but not write it to a response
      val datasets = getDataset(name)

      if (datasets.nonEmpty)
        sender ! Option(datasets.head)
      else
        sender ! None

    case ShowDataset(name) =>
      // Writes metadata about a dataset to the response.
      val datasets = getDataset(name)

      if (datasets.nonEmpty)
        complete(OK, datasets.head)
      else
        complete(NotFound, Map("Status" -> s"""Dataset with name "$name" does not exist."""))

    case ListDatasets() =>
      // Writes metadata about all datasets to the response.
      try {
        val datasets = database withSession {
          implicit session =>
            val query = sql"SELECT * FROM Datasets".as[datasets]
            query.list
        }

        if (datasets.nonEmpty)
          complete(OK, datasets)
        else
          complete(NotFound, Map("Status" -> s"No datasets found."))
      } catch {
        case ex: Exception =>
          complete(NotFound, Map("Status" -> s"No datasets found."))
      }

  }

  def complete[T <: AnyRef : RootJsonFormat](status: StatusCode, obj: T) = {
    rc.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        rc.complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}
