package org.elinker.core.api.scala

import akka.actor.{ActorSystem, Props}
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.EntityLinker
import spray.routing.RequestContext

/**
 * Created by nilesh on 12/10/15.
 */
class FremeNer(config: Config) {
  val classifiers = (for((lang, file) <- config.modelFiles)
    yield (lang, CRFClassifier.getClassifierNoExceptions(file))).toMap

  val system = ActorSystem("api")
  private def entityLinker(implicit requestContext: RequestContext, classifier: CRFClassifier[_]) = system.actorOf(Props(new EntityLinker(requestContext, classifier)))

  def spot(text: String, language: String, outputFormat: String, rdfPrefix: String): String = ???

  def spotClassify(text: String, language: String, outputFormat: String, rdfPrefix: String): String = ???

  def spotLink(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = ???

  def spotLinkClassify(text: String, language: String, dataset: String, outputFormat: String, rdfPrefix: String, numLinks: Int): String = ???
}
