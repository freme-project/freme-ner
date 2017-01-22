package org.elinker.core.api.process

import java.io.ByteArrayInputStream
import java.net.URL
import java.util

import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.rdf.model.{Literal, Model, ModelFactory, Resource}
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.common.SolrInputDocument
import org.elinker.core.api.process.Rest.{RestMessage}
import org.elinker.core.spotter.FremeSpotter

import scala.collection.JavaConversions._

/**
  * Dataset management actor
  *
  * @param solrUri SOLR instance URI where entity URIs and labels are indexed
  *                //@param datasetMetadataDAO Hibernate DatasetSimpleDAO instance for manipulating dataset table
  * @author Nilesh Chakraborty <nilesh@nileshc.com>
  */
class Datasets(solrUri: String) {

  import Datasets._

  //val log = Logging(system, getClass)

  val solr = new HttpSolrClient(solrUri)

  val defaultIndexProps = Seq("http://www.w3.org/2004/02/skos/core#prefLabel",
    "http://www.w3.org/2004/02/skos/core#altLabel",
    "http://www.w3.org/2000/01/rdf-schema#label")

  def indexData(dataset: String, format: String, body: InputType, defaultLang: String, properties: Seq[String], spotter:FremeSpotter): Long = {

    def isIndexed(resource: Resource, label: Literal): Boolean = {

      val uri = resource.getURI
      val language = label.getLanguage


      val query = new SolrQuery()
      query.set("q", s"""resource:"$uri"~3 AND dataset:"$dataset" AND (language:"$language" OR language:"xx")""")
      val response = solr.query("elinker", query)
      val solrResult = response.getResults

      if (solrResult.isEmpty) {
        return false
      }

      return true
    }


    def addLabel(resource: Resource, label: Literal, spotter:FremeSpotter) = {

      if (!isIndexed(resource, label)) {
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
        spotter.addKey(label.getString)
      }
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

    def indexModel(model: Model, spotter:FremeSpotter) = {
      for (property <- properties) {
        for ((resource, label) <- iterateEntityValues(model, property)) {
          addLabel(resource, label, spotter)
        }
      }
    }

    body match {
      case TextInput(text) =>
        // RDF data sent in POST body
        val model = ModelFactory.createDefaultModel()
        model.read(new ByteArrayInputStream(text.getBytes), null, format)
        indexModel(model, spotter)
      case UrlInput(url) =>
        // Fetch RDF data from given URL
        val model = ModelFactory.createDefaultModel()
        model.read(new URL(url).openStream(), null, format)
        indexModel(model, spotter)
      case SparqlInput(query, endpoint) =>
        // Fetch RDF from SPARQL endpoint
        val qe = QueryExecutionFactory.sparqlService(endpoint, query)
        val results = qe.execSelect()
        for (result <- results) {
          val resource = result.get("res").asResource()
          val label = result.get("label").asLiteral()
          addLabel(resource, label, spotter)
        }
    }

    solr.commit("elinker", true, true)
    val sizeQuery = new SolrQuery()
    sizeQuery.set("q", s"dataset:$dataset")
    sizeQuery.set("rows", "0")
    val result = solr.query("elinker", sizeQuery)
    result.getResults.getNumFound
  }

  def deleteDataset(name: String) = {
    solr.deleteByQuery("elinker", s"dataset:$name")
    solr.commit("elinker")
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
