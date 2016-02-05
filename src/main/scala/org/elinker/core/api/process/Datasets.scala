package org.elinker.core.api.process

import java.io.ByteArrayInputStream
import java.net.URL
import java.util

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy}
import akka.event.Logging
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.rdf.model.{Resource, Literal, Model, ModelFactory}
import com.hp.hpl.jena.shared.{JenaException, SyntaxError}
import eu.freme.common.persistence.dao.DatasetSimpleDAO
import eu.freme.common.persistence.model.DatasetSimple
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.elinker.core.api.process.Rest.{StatusOK, StatusCreated, RestMessage}

import scala.collection.JavaConversions._

/**
 * Dataset management actor
 *
 * @param solrUri SOLR instance URI where entity URIs and labels are indexed
 * @param datasetDAO Hibernate DatasetSimpleDAO instance for manipulating dataset table
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
class Datasets(solrUri: String, datasetDAO: DatasetSimpleDAO) extends Actor {

  import Datasets._
  import JsonImplicits._
  import context._

  val log = Logging(system, getClass)

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


  def createDataset(dataset: CreateDataset): (Long, Long) = {
    val d = toDatasetSimple(dataset)

    val timeStamp = d.getCreationTime

    d.setDescription(dataset.description)
    d.setName(dataset.name)
    println(d.toString)
    datasetDAO.save(d)
    println("Saved")

    (d.getTotalEntities, timeStamp)
  }

  def toDatasetSimple (dataset: CreateDataset) :DatasetSimple = {

    val numEntities = indexData(dataset.name, dataset.format, dataset.data, dataset.defaultLang,
      if (dataset.properties.size != 0) dataset.properties else defaultIndexProps).toInt

    val d = new DatasetSimple()

    d.setName(dataset.name)
    d.setDescription(dataset.description)
    d.setTotalEntities(numEntities)

    d
  }

  def deleteDataset(name: String) = {
    solr.deleteByQuery("elinker", s"dataset:$name")
    solr.commit("elinker")

    val d = datasetDAO.getRepository.findOneByName(name)
    datasetDAO.delete(d)
  }

  def getDataset(name: String): List[Dataset] = {
    val d = datasetDAO.getRepository.findOneByName(name)
    if (d != null)
      List(Dataset(d.getName, d.getDescription, d.getTotalEntities,d.getCreationTime))
    else
      Nil
  }

  def getAllDatasets: List[Dataset] = {
    datasetDAO.getRepository.findAll()
      .map(d => Dataset(d.getName, d.getDescription, d.getTotalEntities,d.getCreationTime)).toList
  }

  def receive = {
    case message @ CreateDataset(name, description, _, _, _, _) =>
      // Check whether dataset already exists before attempting to create a new one.
      val datasets = getDataset(name)
      println(datasets.mkString("\n"))
      if (datasets.isEmpty) {
        try {
          val (numEntities, timeStamp) = createDataset(message)
          sender ! StatusCreated(Dataset(name, description, numEntities, timeStamp))
        } catch {
          case ex: SyntaxError =>
            sender ! ex
          case ex: JenaException =>
            sender ! ex
        }
      } else {
        sender ! new DatasetAlreadyExistsException
      }
      stop(self)

    case UpdateDataset(name, description, format, body, defaultLang, properties) =>
      // NOTE: Updating a dataset adds new labels and does not remove anything. This is technically not equivalent to a
      // PUT-based update but we use this for PUT because it's more convenient for incrementally adding a dataset.
      val (numEntities, timeStamp) = createDataset(CreateDataset(name, description, format, body, defaultLang, properties))
      sender ! StatusOK(Dataset(name, description, numEntities, timeStamp))

    case DeleteDataset(name) =>
      val datasets = getDataset(name)

      if (datasets.nonEmpty) {
        deleteDataset(name)
        sender ! StatusOK("Dataset deleted successfully")
      } else {
        sender ! new DatasetDoesNotExistException()
      }
      stop(self)

    case GetDataset(name) =>
      // Convenience message to fetch metadata about a dataset but not write it to a response
      val datasets = getDataset(name)
      println(datasets.mkString("\n"))

      if (datasets.nonEmpty)
        sender ! StatusOK(datasets.head)
      else
        sender ! new DatasetDoesNotExistException
      stop(self)

    case ListDatasets() =>
      // Writes metadata about all datasets to the response.
      try {
        sender ! StatusOK(getAllDatasets)
      } catch {
        case ex: Exception =>
          sender ! StatusOK(List[Dataset]())
      }
      stop(self)

  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        Restart
      }
    }
}

object Datasets {
  case class Dataset(name: String, description: String, totalEntities: Long, creationTime: Long) extends RestMessage
  case class CreateDataset(name: String, description: String, format: String, data: InputType, defaultLang: String, properties: Seq[String]) extends RestMessage
  case class UpdateDataset(name: String, description: String, format: String, data: InputType, defaultLang: String, properties: Seq[String]) extends RestMessage
  case class DeleteDataset(name: String) extends RestMessage
  case class GetDataset(name: String) extends RestMessage
  case class ListDatasets() extends RestMessage

  abstract class InputType()
  case class TextInput(text: String) extends InputType
  case class UrlInput(url: String) extends InputType
  case class SparqlInput(query: String, endpoint: String) extends InputType

  class DatasetException extends Exception
  class DatasetAlreadyExistsException extends DatasetException
  class DatasetDoesNotExistException extends DatasetException
}