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
import org.elinker.core.api.db.{Tables, DB}
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
object Datasets {
  case class Dataset(name: String, description: String, totalEntities: Long, creationTime: Long)
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

  class DatasetException extends Exception
  class DatasetAlreadyExistsException extends DatasetException
  class DatasetDoesNotExistException extends DatasetException
}

class Datasets(solrUri: String, databaseUri: String) extends Actor with DB {

  import Datasets._
  import JsonImplicits._
  import context._

  val log = Logging(system, getClass)

  override val uri = databaseUri

  val solr = new HttpSolrClient(solrUri)

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
          sender ! Dataset(name, description, numEntities, timeStamp)
        } catch {
          case ex: SyntaxError =>
            sender ! ex
          case ex: JenaException =>
            sender ! ex
        }
      } else {
        sender ! new DatasetAlreadyExistsException
      }

    case UpdateDataset(name, description, format, body, defaultLang, properties) =>
      // NOTE: Updating a dataset adds new labels and does not remove anything. This is technically not equivalent to a
      // PUT-based update but we use this for PUT because it's more convenient for incrementally adding a dataset.
      val (numEntities, timeStamp) = createDataset(CreateDataset(name, description, format, body, defaultLang, properties))
      sender ! Dataset(name, description, numEntities, timeStamp)

    case DeleteDataset(name) =>
      val datasets = getDataset(name)

      if (datasets.nonEmpty) {
        deleteDataset(name)
        datasets.head match {
          case Tables.datasets(Some(name), Some(description), Some(totalEntities), Some(creationTime)) =>
            sender ! Dataset(name, description, totalEntities, creationTime.toInstant.toEpochMilli)
        }
      } else {
        sender ! new DatasetDoesNotExistException
      }

    case GetDataset(name) =>
      // Convenience message to fetch metadata about a dataset but not write it to a response
      val datasets = getDataset(name)

      if (datasets.nonEmpty)
        datasets.head match {
          case Tables.datasets(Some(name), Some(description), Some(totalEntities), Some(creationTime)) =>
            sender ! Dataset(name, description, totalEntities, creationTime.toInstant.toEpochMilli)
        }
      else
        sender ! new DatasetDoesNotExistException

    case ListDatasets() =>
      // Writes metadata about all datasets to the response.
      try {
        val datasets = database withSession {
          implicit session =>
            val query = sql"SELECT * FROM Datasets".as[datasets]
            query.list
        }

        sender ! datasets.map{
          case Tables.datasets(Some(name), Some(description), Some(totalEntities), Some(creationTime)) =>
            Dataset(name, description, totalEntities, creationTime.toInstant.toEpochMilli)
        }
      } catch {
        case ex: Exception =>
          sender ! List[Dataset]()
      }

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        Stop
      }
    }
}
