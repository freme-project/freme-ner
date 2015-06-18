package org.elinker.core.api.service

import akka.actor.Props
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.EntityLinker
import spray.routing.{HttpService, RequestContext}

/**
 * Created by nilesh on 03/06/15.
 */
trait EntityApiService  extends HttpService {
  def entityLinker(implicit requestContext: RequestContext, classifier: CRFClassifier[_]) = actorRefFactory.actorOf(Props(new EntityLinker(requestContext, classifier)))

  val classifiers = Map(("en", CRFClassifier.getClassifierNoExceptions("/home/nilesh/elinker/wikiner-en-ner-model.ser.gz")),
    ("de", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/german.dewac_175m_600.crf.ser.gz")),
    ("fr", CRFClassifier.getClassifierNoExceptions("/home/nilesh/elinker/wikiner-fr-ner-model.ser.gz")),
    ("es", CRFClassifier.getClassifierNoExceptions("/home/nilesh/elinker/wikiner-es-ner-model.ser.gz"))
  )

//  val classifiers = Map(("en", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz")))

  def entityRoute =
    (path("entities") & post) {
          parameter("language" ? "en") {
            language =>
              entity(as[String]) {
                text =>
                  implicit requestContext: RequestContext =>
                    implicit val classifier = classifiers(language)
                    entityLinker ! EntityLinker.Text(text, language)
              }
          }
    }
}