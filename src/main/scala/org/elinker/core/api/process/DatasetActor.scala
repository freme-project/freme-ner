package org.elinker.core.api.process

import java.io.ByteArrayInputStream

import akka.actor.Actor
import akka.event.Logging
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.elinker.core.api.db.DB
import spray.http.StatusCodes
import spray.routing.RequestContext

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}

/**
* Created by nilesh on 16/12/2014.
*/
object DatasetActor {
  case class CreateDataset(name: String, format: String, body: String)
  case class UpdateDataset(name: String, format: String, body: String)
  case class DeleteDataset(name: String)
  case class ShowDataset(name: String)
  case class ListDatasets()
}

class DatasetActor(rc: RequestContext) extends Actor with DB {
  import DatasetActor._
  import org.elinker.core.api.db.Tables._

  implicit val system = context.system

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient("http://localhost:8983/solr")

  val jsonMapper = new ObjectMapper() with ScalaObjectMapper
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerSubtypes(classOf[datasets])

  val indexProperties = Seq("http://www.w3.org/2004/02/skos/core#:prefLabel",
  "http://www.w3.org/2004/02/skos/core#:altLabel")

  def indexData(dataset: String, format: String, body: String): Long = {
    val model = ModelFactory.createDefaultModel()
    model.read(new ByteArrayInputStream(body.getBytes), null, format)
    val resources = mutable.Set[String]()

    for(property <- indexProperties) {
      val field = property.split("#:")(1)
      for ((uri, label) <- iterateEntityValues(model, property)) {
        val document = new SolrInputDocument()
        document.addField("dataset", dataset)
        document.addField("resource", uri)
        document.addField(field, label)
        resources += uri
        solr.add("elinker", document)
      }
    }

    solr.commit("elinker", true, true)

    resources.size
  }

  def iterateEntityValues(model: Model, property: String): Traversable[(String, String)] = {
    val iter = model.listStatements(null, model.getProperty(property), null)
    new Traversable[(String, String)] {
      override def foreach[U](f: ((String, String)) => U): Unit = {
        for(statement <- iter) {
          f((statement.getSubject.asResource().getURI, statement.getObject.asLiteral().toString))
        }
      }
    }
  }

  def createDataset(dataset: CreateDataset): (Long, Long) = {
    val numEntities = indexData(dataset.name, dataset.format, dataset.body)
    val timeStamp = System.currentTimeMillis()
    database withSession {
      implicit session =>
        sqlu"""
                 INSERT INTO Datasets VALUES (${dataset.name}, $numEntities, $timeStamp)
              """.first
    }
    (numEntities, timeStamp)
  }

  def deleteDataset(name: String) = {
    database withSession {
      implicit session =>
        sqlu"""
                 DELETE FROM Datasets WHERE name = $name
              """.first
    }
    solr.deleteByQuery("elinker", s"dataset:$name")
    solr.commit("elinker")
  }

  def receive = {
    case message @ CreateDataset(name, _, _) =>
      val (numEntities, timeStamp) = createDataset(message)
      rc.complete(StatusCodes.Created, jsonMapper.writeValueAsString(Map(("Dataset", name),
        ("TotalEntities", numEntities),
        ("CreationTime", timeStamp))))

    case UpdateDataset(name, format, body) =>
      deleteDataset(name)
      val (numEntities, timeStamp) = createDataset(CreateDataset(name, format, body))
      rc.complete(jsonMapper.writeValueAsString(Map(("Dataset", name),
        ("TotalEntities", numEntities),
        ("CreationTime", timeStamp))))

    case DeleteDataset(name) =>
      val datasets = database withSession {
        implicit session =>
          val query = Q.query[String, datasets]("SELECT * FROM Datasets WHERE name = ?")
          query(name).list
      }

      if(datasets.nonEmpty) {
        deleteDataset(name)
        rc.complete(StatusCodes.OK)
      } else {
        rc.complete(StatusCodes.NotFound)
      }

    case ShowDataset(name) =>
      val datasets = database withSession {
        implicit session =>
          val query = Q.query[String, datasets]("SELECT * FROM Datasets WHERE name = ?")
          query(name).list
      }

      if(datasets.nonEmpty)
        rc.complete(jsonMapper.writeValueAsString(datasets.head))
      else
        rc.complete(StatusCodes.NotFound)

    case ListDatasets() =>
      try {
        val datasets = database withSession {
          implicit session =>
            val query = sql"SELECT * FROM Datasets".as[datasets]
            query.list
        }

        if(datasets.nonEmpty)
          rc.complete(jsonMapper.writeValueAsString(datasets))
        else
          rc.complete(StatusCodes.NotFound)
      } catch {
        case ex: Exception =>
          rc.complete(StatusCodes.NotFound)
      }

  }
}
