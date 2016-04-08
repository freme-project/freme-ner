package org.elinker.core.api.scala

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.Rest.EnrichedOutput
import org.elinker.core.api.process.{Datasets, DomainMap, EntityLinker}
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * FremeNER Scala API for performing spotting, linking and dataset management.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
class FremeNer(override val getConfig: Config) extends DomainMap{
  import FremeNer._

  val classifiers = (for((lang, file) <- getConfig.modelFiles)
    yield (lang, CRFClassifier.getClassifierNoExceptions(file))).toMap

  val system = ActorSystem("api")
  private def entityLinker(implicit classifier: CRFClassifier[_], config: Config) = system.actorOf(Props(new EntityLinker(classifier, config.solrURI, config.sparqlEndpoint)))
  private def datasets(implicit config: Config) = new Datasets(config.solrURI) //system.actorOf(Props(new Datasets(config.solrURI/*, config.datasetDAO*/)))

  implicit val timeout = Timeout(5 seconds)
  implicit val configImpl = getConfig

  def spot(text: String, language: String, outputFormat: String, rdfPrefix: String): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = false),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotClassify(text: String, language: String, outputFormat: String, rdfPrefix: String): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = true),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, Set(), classify = false),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, types: Set[String]): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, types, classify = false),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, domain: String): String = {
    implicit val classifier = classifiers(language)
    val types = domains(domain)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, types, classify = false),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, Set(), classify = true),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, types: Set[String]): String = {
    implicit val classifier = classifiers(language)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, types, classify = true),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, domain: String): String = {
    implicit val classifier = classifiers(language)
    val types = domains(domain)
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, types, classify = true),
      timeout.duration) match {
      case EnrichedOutput(output: String) => output
    }
  }

  def addToDataset(name: String, dataset: InputType, format: String, language: String, properties: Array[String]): Long = {
    dataset match {
      case TextInput(text) =>
        datasets.indexData(name, format, Datasets.TextInput(text), language, if (properties.size != 0) properties else datasets.defaultIndexProps)
      case UrlInput(url) =>
        datasets.indexData(name, format, Datasets.UrlInput(url), language, if (properties.size != 0) properties else datasets.defaultIndexProps)
      case SparqlInput(query, endpoint) =>
        datasets.indexData(name, format, Datasets.SparqlInput(query, endpoint), language, if (properties.size != 0) properties else datasets.defaultIndexProps)
    }

  }

  def deleteDataset(name: String): Unit = {
    datasets.deleteDataset(name)
  }
}

object FremeNer {
  abstract class InputType()
  case class TextInput(text: String) extends InputType
  case class UrlInput(url: String) extends InputType
  case class SparqlInput(query: String, endpoint: String) extends InputType
}