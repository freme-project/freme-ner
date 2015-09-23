package org.elinker.core.api.service

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import spray.routing.{HttpService, RequestContext}

import scala.collection.JavaConversions._

/**
 * Created by nilesh on 03/06/15.
 */
case class Result(word: String, label: String)

trait MentionApiService  extends HttpService {
//  def mentionActor(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new EntityMentions(requestContext)))
  val classifiers = Map(("en", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz")),
  ("de", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/german.dewac_175m_600.crf.ser.gz")),
  ("es  ", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz"))
)

  val jsonMapper = new ObjectMapper() with ScalaObjectMapper
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerSubtypes(classOf[Result])

  def mentionRoute =
    path("mentions" / Segment) {
      language =>
//        get {
//          path(Segment) {
//            text =>
//              implicit requestContext: RequestContext =>
//                val classify = classifiers(language).classify(text).asInstanceOf[util.List[util.List[CoreLabel]]]
//                val results = new util.ArrayList[Result]
//                for(coreLabels <- classify) {
//                  for(coreLabel <- coreLabels) {
//                    val word = coreLabel.word()
//                    val label = coreLabel.get(classOf[CoreAnnotations.AnswerAnnotation])
//                    if(!"O".equals(label)){
//                      results.add(new Result(word, label))
//                    }
//                  }
//                }
//                requestContext.complete(jsonMapper.writeValueAsString(results.toList))
////                mentionActor ! EntityMentions.Text(text, language)
//          }
//        } ~
          post {
            entity(as[String]) {
              text =>
                implicit requestContext: RequestContext =>
                  val classify = classifiers(language).classify(text).asInstanceOf[util.List[util.List[CoreLabel]]]
                  val results = new util.ArrayList[Result]
                  for(coreLabels <- classify) {
                    for(coreLabel <- coreLabels) {
                      val word = coreLabel.word()
                      val label = coreLabel.get(classOf[CoreAnnotations.AnswerAnnotation])
                      if(!"O".equals(label)){
                        results.add(new Result(word, label))
                      }
                    }
                  }
                  requestContext.complete(jsonMapper.writeValueAsString(results.toList))
//                  mentionActor ! EntityMentions.Text(text, language)
            }
          }
    }
}