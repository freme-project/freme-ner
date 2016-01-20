package org.elinker.core.api.process

import java.io.{ByteArrayOutputStream, StringWriter}
import java.util

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{OneForOneStrategy, Actor}
import akka.event.Logging
import edu.stanford.nlp.ie.crf.{CRFCliqueTree, CRFClassifier}
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.util.ClientUtils
import org.elinker.core.api.java.serialize.{NIFParser, NIFConverter}
import org.elinker.core.api.java.utils.SPARQLProcessor
import org.elinker.core.api.process.Rest.{EnrichedOutput, RestMessage}
import spray.http.StatusCodes._
import spray.routing.RequestContext
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
* Created by nilesh on 16/12/2014.
*/
object EntityLinker {
  case class SpotLinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String, numLinks: Int, types: Set[String], classify: Boolean) extends RestMessage
  case class SpotEntities(text: String, language: String, outputFormat: String, prefix: String, classify: Boolean) extends RestMessage
  case class LinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String) extends RestMessage
}

class EntityLinker[T <: CoreMap](nerClassifier: CRFClassifier[T], solrURI: String) extends Actor {
  import EntityLinker._
  import context._

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient(solrURI)

  private val parser = new NIFParser()

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
    (for (sentence <- nerClassifier.classify(text)) yield {
//      println(sentence)
      val p = nerClassifier.documentToDataAndLabels(sentence)
      val cliqueTree: CRFCliqueTree[String] = nerClassifier.getCliqueTree(p)
//      val entities = ListBuffer[(Int, Int, String, Double)]()

      var currentBegin = 0
      var currentEnd = 0
      var tokensInCurrentEntity = 0
      var currentClassLabel = "O"
      var currentProbs = 0.0
      var entityMention = ""

      val entities = (for ((doc, i) <- sentence.zipWithIndex;
            mention = doc.get(classOf[CoreAnnotations.TextAnnotation]);
            begin = doc.get(classOf[CoreAnnotations.CharacterOffsetBeginAnnotation]);
            end = doc.get(classOf[CoreAnnotations.CharacterOffsetEndAnnotation]);
            (classLabel, prob) = (for ((classLabel, j) <- nerClassifier.classIndex.objectsList().zipWithIndex) yield (classLabel, cliqueTree.prob(i, j))).maxBy(_._2)
      ) yield {
//          println(mention + " " + begin + " " + end)
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
            if (classLabel != "O") {
              if (tokensInCurrentEntity == 0) currentBegin = begin
              tokensInCurrentEntity += 1
              currentEnd = end
              currentClassLabel = classLabel
              entityMention = if (entityMention.isEmpty) mention else entityMention + " " + mention
              currentProbs += prob
            }
            Nil
          }
        }).flatten

      if(tokensInCurrentEntity > 0)
        entities += Result(currentClassLabel, entityMention, currentBegin, currentEnd, None, Some(currentProbs / tokensInCurrentEntity))

      entities
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

  val sparqlProc = new SPARQLProcessor()
  def getDbpediaTypes(uri: String): Set[String] = sparqlProc.getTypes(uri).toSet

  def receive = {
    case SpotEntities(text, language, outputFormat, prefix, classify) =>
//      println(text)
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
      val out = new ByteArrayOutputStream()
      contextModel.write(out, outputFormat)
      sender ! EnrichedOutput(out.toString("UTF-8"))
      stop(self)

    case SpotLinkEntities(text, language, outputFormat, dataset, prefix, numLinks, types, classify) =>
      val results = getEntities(text, language, dataset, numLinks)

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)

      results.foreach {
        case Result(entityType, mention, begin, end, taIdentRef, score) =>
          val mentionModel = (taIdentRef, score) match {
            case (Some(ref), Some(s)) if numLinks == 1 =>
              if(types.isEmpty || types.intersect(getDbpediaTypes(ref)).nonEmpty) {
                if(classify)
                  nif.createLinkWithTypeAndScore(entityType, mention, begin, end, ref, s, contextRes)
                else
                  nif.createLinkWithScore(mention, begin, end, ref, s, contextRes)
              } else {
                null
              }
            case (Some(ref), Some(s)) =>
              if(types.isEmpty || types.intersect(getDbpediaTypes(ref)).nonEmpty) {
                if (classify)
                  nif.createLinkWithType(entityType, mention, begin, end, ref, contextRes)
                else
                  nif.createLink(mention, begin, end, ref, contextRes)
              } else {
                null
              }
            case (None, Some(score)) =>
              if(classify)
                nif.createMentionWithTypeAndScore(entityType, mention, begin, end, score, contextRes)
              else
                nif.createMentionWithScore(mention, begin, end, score, contextRes)
          }

          // Merge the context and the mention.
          if(mentionModel != null) contextModel.add(mentionModel)
      }

      // Convert the model to String.
      val out = new ByteArrayOutputStream()
      contextModel.write(out, outputFormat)
      sender ! EnrichedOutput(out.toString("UTF-8"))
      stop(self)

    case LinkEntities(nifString, language, outputFormat, dataset, prefix) =>
      val document = parser.getDocumentFromNIFString(nifString)
      val text = document.getText
      val annotations = document.getEntities

      val nif = new NIFConverter(prefix)
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)

      for(annotation <- annotations;
          begin = annotation.getBeginIndex;
          end = annotation.getEndIndex;
          mention = annotation.getMention;
          ref = linkToKB(mention, dataset, language, 1)
          if ref.nonEmpty
      ) {
        contextModel.add(nif.createLink(mention, begin, end, ref.head._1, contextRes))
      }

      // Convert the model to String.
      val out = new ByteArrayOutputStream()
      contextModel.write(out, outputFormat)
      sender ! EnrichedOutput(out.toString("UTF-8"))
      stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        Stop
      }
    }
}

case class Result(entityType: String, mention: String, beginIndex: Int, endIndex: Int, taIdentRef: Option[String], score: Option[Double])