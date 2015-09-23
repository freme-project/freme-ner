package org.elinker.core.api.process

import java.io.StringWriter
import java.util

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, Actor}
import akka.event.Logging
import com.fasterxml.jackson.databind.ObjectMapper
import com.hp.hpl.jena.vocabulary.RDF
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.aksw.gerbil.transfer.nif.data.{SpanImpl, NamedEntity}
import org.aksw.gerbil.transfer.nif._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.util.ClientUtils
import org.elinker.serialize.NIFConverter
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.json.RootJsonFormat
import spray.routing.RequestContext

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
* Created by nilesh on 16/12/2014.
*/
object EntityLinker {
  case class SpotLinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String, numLinks: Int, classify: Boolean)
  case class SpotEntities(text: String, language: String, outputFormat: String, prefix: String, classify: Boolean)
  case class LinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String)
  case class GerbilAnnotate(nif: String, language: String, dataset: String)
  case class GerbilSpot(nif: String, language: String)
  case class GerbilDisambiguate(nif: String, language: String, dataset: String)
}

class EntityLinker(rc: RequestContext, nerClassifier: CRFClassifier[_]) extends Actor {
  import EntityLinker._
  import context._

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient("http://localhost:8983/solr")

  private val parser = new TurtleNIFDocumentParser()
  private val creator = new TurtleNIFDocumentCreator()

  def linkToKB(mention: String, dataset: String, language: String, maxLinks: Int): Seq[(String, Double)] = {
    // Find links to URIs in datasets by querying SOLR index
    def e(s: String) = ClientUtils.escapeQueryChars(s)

    val query = new SolrQuery()
    query.set("q", s"""label:"${e(mention)}" AND dataset:"$dataset" AND (language:"$language" OR language:"xx")""")
    query.set("fl", "*,score")
    query.set("sort", "score desc, count desc")
    query.set("rows", 10)

    val response = solr.query("elinker", query)
    val results = response.getResults
    val highlights = response.getHighlighting
    val maxScore = results.getMaxScore

    if(results.isEmpty) {
      Nil
    } else {
      results.map {
        case document =>
          val resource = document.get("resource").asInstanceOf[String]
          val relevance = document.get("score").asInstanceOf[Float].toDouble / maxScore
          (resource, relevance)
      }.take(maxLinks).toSeq
    }
  }

  def getMentions(text: String, language: String): List[Result] = {
    // Fetch entity mentions in text (only spotting)
    val results = new ListBuffer[Result]()
    val triples = nerClassifier.classifyToCharacterOffsets(text)
    for(triple <- triples) {
      val entityType = triple.first()
      val begin = triple.second()
      val end = triple.third()
      val phrase = text.substring(begin, end)
      results.add(Result(entityType, phrase, begin, end, None, None))
    }
//    println(results.mkString("\n"))
    results.toList
  }

  def getEntities(text: String, language: String, dataset: String, linksPerMention: Int): List[Result] = {
    // Spot entities and link to given dataset
    val results = new ListBuffer[Result]()
    val triples = nerClassifier.classifyToCharacterOffsets(text)
    for(triple <- triples) {
      val entityType = triple.first()
      val begin = triple.second()
      val end = triple.third()
      val phrase = text.substring(begin, end)
      val links = linkToKB(phrase, dataset, language, linksPerMention)

      if(links.isEmpty) {
        results.add(Result(entityType, phrase, begin, end, None, None))
      } else {
        for ((link, score) <- links) {
          results.add(Result(entityType, phrase, begin, end, Some(link), Some(score)))
        }
      }
    }
    results.toList
  }

  def outputAnnotatedDocument(document: Document, annotations: util.List[Marking]): Unit = {
    document.setMarkings(annotations)
    val nifDocument = creator.getDocumentAsNIFString(document)
    rc.complete(OK, nifDocument)
    stop(self)
  }

  def receive = {
    case SpotEntities(text, language, outputFormat, prefix, classify) =>
      val results = getMentions(text, language)

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)


      results.foreach {
        case Result(entityType, mention, begin, end, _, _) =>
          val mentionModel = if(classify)
            nif.createMentionWithType(entityType, mention, begin, end, contextRes)
          else
            nif.createMention(mention, begin, end, contextRes)

          // Merge the context and the mention.
          contextModel.add(mentionModel)
      }

      // Convert the model to String.
      val writer = new StringWriter()
      contextModel.write(writer, outputFormat)
      rc.complete(OK, writer.toString)
      stop(self)

    case SpotLinkEntities(text, language, outputFormat, dataset, prefix, numLinks, classify) =>
      val results = getEntities(text, language, dataset, numLinks)

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)

      results.foreach {
        case Result(entityType, mention, begin, end, taIdentRef, score) =>
          val mentionModel = (taIdentRef, score) match {
            case (Some(ref), Some(s)) if numLinks == 1 =>
              if(classify)
                nif.createLinkWithTypeAndScore(entityType, mention, begin, end, ref, s, contextRes)
              else
                nif.createLinkWithScore(mention, begin, end, ref, s, contextRes)
            case (Some(ref), Some(s)) =>
              if(classify)
                nif.createLinkWithType(entityType, mention, begin, end, ref, contextRes)
              else
                nif.createLink(mention, begin, end, ref, contextRes)
            case _ =>
              if(classify)
                nif.createMentionWithType(entityType, mention, begin, end, contextRes)
              else
                nif.createMention(mention, begin, end, contextRes)
          }

          // Merge the context and the mention.
          contextModel.add(mentionModel)
      }

      // Convert the model to String.
      val writer = new StringWriter()
      contextModel.write(writer, outputFormat)
      rc.complete(OK, writer.toString)
      stop(self)

    case LinkEntities(nifString, language, outputFormat, dataset, prefix) =>
      val document = parser.getDocumentFromNIFString(nifString)
      val text = document.getText
      val spans = document.getMarkings(classOf[Span])
      val annotations = new util.ArrayList[Marking](spans.size)

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)

      for(span <- spans;
          begin = span.getStartPosition;
          end = begin + span.getLength;
          mention = text.substring(begin, end);
          ref = linkToKB(mention, dataset, language, 1)
          if ref.nonEmpty
      ) yield {
        contextModel.add(nif.createLink(mention, begin, end, ref.head._1, contextRes))
      }

      // Convert the model to String.
      val writer = new StringWriter()
      contextModel.write(writer, outputFormat)
      rc.complete(OK, writer.toString)
      stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        rc.complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}

case class Result(entityType: String, mention: String, beginIndex: Int, endIndex: Int, taIdentRef: Option[String], score: Option[Double])