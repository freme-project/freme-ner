package org.elinker.core.api.process

import java.io.StringWriter
import java.util

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, Actor}
import akka.event.Logging
import edu.stanford.nlp.ie.crf.{CRFCliqueTree, CRFClassifier}
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.aksw.gerbil.transfer.nif._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.util.ClientUtils
import org.elinker.serialize.NIFConverter
import spray.http.StatusCodes._
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

class EntityLinker[T <: CoreMap](rc: RequestContext, nerClassifier: CRFClassifier[T]) extends Actor {
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
    query.set("q", s"""label:"${e(mention)}"~3 AND dataset:"$dataset" AND (language:"$language" OR language:"xx")""")
    query.set("sort", "score desc, count desc")
    query.set("rows", 10)

    val response = solr.query("elinker", query)
    val results = response.getResults

    if(results.isEmpty) {
      Nil
    } else {
      results.map {
        case document =>
          val resource = document.get("resource").asInstanceOf[String]
          val relevance = 0.0
          (resource, relevance)
      }.take(maxLinks).toSeq
    }
  }


  def getMentions(text: String): Seq[Result] = {
    // Fetch entity mentions in text (only spotting) along with confidence scores
    val sentences = nerClassifier.classify(text).get(0)
    val p = nerClassifier.documentToDataAndLabels(sentences)
    val cliqueTree: CRFCliqueTree[String] = nerClassifier.getCliqueTree(p)
    val entities = ListBuffer[(Int, Int, String, Double)]()

    var currentBegin = 0
    var currentEnd = 0
    var tokensInCurrentEntity = 0
    var currentClassLabel = "O"
    var currentProbs = 0.0
    var entityMention = ""

    (for((doc, i) <- sentences.zipWithIndex;
         mention = doc.get(classOf[CoreAnnotations.TextAnnotation]);
         begin = doc.get(classOf[CoreAnnotations.CharacterOffsetBeginAnnotation]);
         end = doc.get(classOf[CoreAnnotations.CharacterOffsetEndAnnotation]);
         (classLabel, prob) = (for((classLabel, j) <- nerClassifier.classIndex.objectsList().zipWithIndex) yield (classLabel, cliqueTree.prob(i, j))).maxBy(_._2)
    ) yield {
        if (currentClassLabel != classLabel && currentClassLabel != "O") {
          val result = Result(currentClassLabel, entityMention, currentBegin, currentEnd, None, Some(currentProbs / tokensInCurrentEntity))
          currentBegin = 0
          currentEnd = 0
          tokensInCurrentEntity = 0
          currentClassLabel = classLabel
          entityMention = ""
          currentProbs = 0.0
          Seq(result)
        } else {
          if(classLabel != "O") {
            if(tokensInCurrentEntity == 0) currentBegin = begin
            tokensInCurrentEntity += 1
            currentEnd = end
            currentClassLabel = classLabel
            entityMention = if(entityMention.isEmpty) mention else entityMention + " " + mention
            currentProbs += prob
          }
          Nil
        }
      }).flatten
  }

  def getEntities(text: String, language: String, dataset: String, linksPerMention: Int): Seq[Result] = {
    // Spot entities and link to given dataset
    (for(result @ Result(entityType, phrase, begin, end, _, Some(score)) <- getMentions(text)) yield {
      val links = linkToKB(phrase, dataset, language, linksPerMention)
      if(links.isEmpty)
        Seq(result)
      else
        for((link, _) <- links) yield result.copy(taIdentRef = Some(link))
    }).flatten
  }

  def outputAnnotatedDocument(document: Document, annotations: util.List[Marking]): Unit = {
    document.setMarkings(annotations)
    val nifDocument = creator.getDocumentAsNIFString(document)
    rc.complete(OK, nifDocument)
    stop(self)
  }

  def receive = {
    case SpotEntities(text, language, outputFormat, prefix, classify) =>
      val results = getMentions(text)

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)


      results.foreach {
        case Result(entityType, mention, begin, end, _, Some(score)) =>
          val mentionModel = if(classify)
            nif.createMentionWithTypeAndScore(entityType, mention, begin, end, score, contextRes)
          else
            nif.createMentionWithScore(mention, begin, end, score, contextRes)

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
            case (None, Some(score)) =>
              if(classify)
                nif.createMentionWithTypeAndScore(entityType, mention, begin, end, score, contextRes)
              else
                nif.createMentionWithScore(mention, begin, end, score, contextRes)
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
      ) {
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