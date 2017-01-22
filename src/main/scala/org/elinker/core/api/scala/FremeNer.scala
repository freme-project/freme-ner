package org.elinker.core.api.scala

import java.util.concurrent.TimeoutException

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import edu.stanford.nlp.ie.crf.CRFClassifier
import eu.freme.common.exception.ExternalServiceFailedException
import eu.freme.common.messages.Messages
import org.elinker.core.api.process.Rest.EnrichedOutput
import org.elinker.core.api.process.{Datasets, DomainMap, EntityLinker}
import org.elinker.core.spotter.FremeSpotter

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * FremeNER Scala API for performing spotting, linking and dataset management.
  *
  * @author Nilesh Chakraborty <nilesh@nileshc.com>
  */
class FremeNer(override val getConfig: Config) extends DomainMap {

  import FremeNer._

  val classifiers = (for ((lang, file) <- getConfig.modelFiles)
    yield (lang, CRFClassifier.getClassifierNoExceptions(file))).toMap

  val system = ActorSystem("api")
  implicit val timeout = Timeout(5 minutes)
  implicit val configImpl = getConfig

  def spot(text: String, language: String, outputFormat: String, rdfPrefix: String, nifVersion:String): String = {
    implicit val classifier = classifiers(language)
    try {
      Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = false, nifVersion),
        timeout.duration) match {
        case EnrichedOutput(output: String) => output
      }
    } catch {
      case futureTimeOut: TimeoutException => {
        throw new ExternalServiceFailedException(Messages.SERVER_TO_BUSY)
      }
    }
  }

  def spotClassify(text: String, language: String, outputFormat: String, rdfPrefix: String, nifVersion:String): String = {
    implicit val classifier = classifiers(language)
    try {
      Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = true, nifVersion),
        timeout.duration) match {
        case EnrichedOutput(output: String) => output
      }
    } catch {
      case futureTimeOut: TimeoutException => {
        throw new ExternalServiceFailedException(Messages.SERVER_TO_BUSY)
      }
    }
  }

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, domain: String, types: String, linkingMethod: String, nifVersion:String): String = {
    implicit val classifier = classifiers(language)
    val restrictToTypes = {
      val domainTypes = if (domain.nonEmpty) domains.getOrElse(domain, Set[String]()) else Set[String]()
      val filterTypes = if (types.nonEmpty) types.split(",").toSet else Set[String]()
      if (domainTypes.isEmpty && filterTypes.isEmpty)
        domainTypes
      else if (domainTypes.isEmpty)
        filterTypes
      else if (filterTypes.isEmpty)
        domainTypes
      else domainTypes.intersect(filterTypes)
    }
    try {
      Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, restrictToTypes, classify = false, linkingMethod, nifVersion),
        timeout.duration) match {
        case EnrichedOutput(output: String) => output
      }
    } catch {
      case futureTimeOut: TimeoutException => {
        throw new ExternalServiceFailedException(Messages.SERVER_TO_BUSY)
      }
    }
  }

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, domain: String, types: String, linkingMethod: String, nifVersion:String): String = {
    implicit val classifier = classifiers(language)
    val restrictToTypes = {
      val domainTypes = if (domain.nonEmpty) domains.getOrElse(domain, Set[String]()) else Set[String]()
      val filterTypes = if (types.nonEmpty) types.split(",").toSet else Set[String]()
      if (domainTypes.isEmpty && filterTypes.isEmpty)
        domainTypes
      else if (domainTypes.isEmpty)
        filterTypes
      else if (filterTypes.isEmpty)
        domainTypes
      else domainTypes.intersect(filterTypes)
    }
    try {
      Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, restrictToTypes, classify = true, linkingMethod, nifVersion),
        timeout.duration) match {
        case EnrichedOutput(output: String) => output
      }
    } catch {
      case futureTimeOut: TimeoutException => {
        throw new ExternalServiceFailedException(Messages.SERVER_TO_BUSY)
      }
    }
  }

  private def entityLinker(implicit classifier: CRFClassifier[_], config: Config) = system.actorOf(Props(new EntityLinker(classifier, config.solrURI, config.sparqlEndpoint)))

  def link(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int, domain: String, types: String, linkingMethod: String, nifVersion:String): String = {
    implicit val classifier = classifiers(language)
    val restrictToTypes = {
      val domainTypes = if (domain.nonEmpty) domains.getOrElse(domain, Set[String]()) else Set[String]()
      val filterTypes = if (types.nonEmpty) types.split(",").toSet else Set[String]()
      if (domainTypes.isEmpty && filterTypes.isEmpty)
        domainTypes
      else if (domainTypes.isEmpty)
        filterTypes
      else if (filterTypes.isEmpty)
        domainTypes
      else domainTypes.intersect(filterTypes)
    }
    try {
      Await.result(entityLinker ? EntityLinker.LinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, restrictToTypes, linkingMethod, nifVersion),
        timeout.duration) match {
        case EnrichedOutput(output: String) => output
      }
    } catch {
      case futureTimeOut: TimeoutException => {
        throw new ExternalServiceFailedException(Messages.SERVER_TO_BUSY)
      }
    }
  }

  def addToDataset(name: String, dataset: InputType, format: String, language: String, properties: Array[String], spotter:FremeSpotter): Long = {
    dataset match {
      case TextInput(text) =>
        datasets.indexData(name, format, Datasets.TextInput(text), language, if (properties.length != 0) properties else datasets.defaultIndexProps, spotter)
      case UrlInput(url) =>
        datasets.indexData(name, format, Datasets.UrlInput(url), language, if (properties.length != 0) properties else datasets.defaultIndexProps, spotter)
      case SparqlInput(query, endpoint) =>
        datasets.indexData(name, format, Datasets.SparqlInput(query, endpoint), language, if (properties.length != 0) properties else datasets.defaultIndexProps, spotter)
    }

  }

  def deleteDataset(name: String): Unit = {
    datasets.deleteDataset(name)
  }

  private def datasets(implicit config: Config) = new Datasets(config.solrURI)
}

object FremeNer {

  abstract class InputType()

  case class TextInput(text: String) extends InputType

  case class UrlInput(url: String) extends InputType

  case class SparqlInput(query: String, endpoint: String) extends InputType

}