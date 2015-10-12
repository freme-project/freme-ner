package org.elinker.core.api.scala

import akka.actor.{ActorSystem, Props}
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.EntityLinker
import spray.routing.RequestContext
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

/**
 * Created by nilesh on 12/10/15.
 */
class FremeNer(config: Config) {
  val classifiers = (for((lang, file) <- config.modelFiles)
    yield (lang, CRFClassifier.getClassifierNoExceptions(file))).toMap

  val system = ActorSystem("api")
  private def entityLinker(implicit requestContext: RequestContext, classifier: CRFClassifier[_]) = system.actorOf(Props(new EntityLinker(requestContext, classifier)))

  implicit val timeout = Timeout(5 seconds)

  def spot(text: String, language: String, outputFormat: String, rdfPrefix: String): String = {
    Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = false),
      timeout.duration).asInstanceOf[String]
  }

  def spotClassify(text: String, language: String, outputFormat: String, rdfPrefix: String): String = {
    Await.result(entityLinker ? EntityLinker.SpotEntities(text, language, outputFormat, rdfPrefix, classify = true),
      timeout.duration).asInstanceOf[String]
  }

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = {
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, classify = false),
      timeout.duration).asInstanceOf[String]
  }

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = {
    Await.result(entityLinker ? EntityLinker.SpotLinkEntities(text, language, outputFormat, dataset, rdfPrefix, numLinks, classify = true),
      timeout.duration).asInstanceOf[String]
  }
}
