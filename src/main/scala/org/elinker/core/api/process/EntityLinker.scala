package org.elinker.core.api.process

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy}
import akka.event.Logging
import edu.stanford.nlp.ie.crf.{CRFClassifier, CRFCliqueTree}
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.util.ClientUtils
import org.apache.solr.common.SolrDocumentList
import org.elinker.core.api.filter.SimilarityFilter
import org.elinker.core.api.java.serialize.NIFParser
import org.elinker.core.api.java.utils.SPARQLProcessor
import org.elinker.core.api.process.Rest.{EnrichedOutput, RestMessage}
import org.nlp2rdf.NIFWrapper

import scala.collection.JavaConversions._

/**
  * Actor for enriching raw text or NIF documents with entities, classes and URIs.
  *
  * @param nerClassifier  Stanford CoreNLP CRFClassifier created from a particular NER model
  * @param solrURI        SOLR instance URI where entity URIs and labels are indexed
  * @param sparqlEndpoint SPARQL endpoint URI for retrieving rdf:type of entities
  * @author Nilesh Chakraborty <nilesh@nileshc.com>
  * @todo Replace all printlns with proper logging.
  */
class EntityLinker[T <: CoreMap](nerClassifier: CRFClassifier[T], solrURI: String, sparqlEndpoint: String) extends Actor {

  import EntityLinker._
  import context._

  implicit val system = context.system
  implicit val executionContext = system.dispatcher

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient(solrURI)

  val similarityFilter = new SimilarityFilter(solr)

  private val parser = new NIFParser()

  /**
    * Disambiguate an entity mention against a knowledge base. Follows a naive approach currently: query for the mention
    * against a SOLR index of URIs, surface forms and their respective usage counts on Wikipedia, and pick the most common
    * sense from the candidates.
    *
    * @param mention  String spotted by nerClassifier
    * @param datasets DatasetMetadata name (eg. dbpedia)
    * @param language Language code, eg. en
    * @param maxLinks Maximum number of URIs to fetch (top-N)
    * @return Seq of (URI, confidence score)
    */
  private def linkToKB(mention: String, datasets: String, language: String, maxLinks: Int): Seq[(String, Double)] = {
    // Find links to URIs in datasets by querying SOLR index
    def e(s: String) = ClientUtils.escapeQueryChars(s)

    val query = new SolrQuery()

    val results = new SolrDocumentList

    datasets.split(",").foreach(dataset => {

      query.set("q", s"""label:"${e(mention)}"~3 AND dataset:"$dataset" AND (language:"$language" OR language:"xx")""")
      query.set("sort", "score desc, count desc")
      query.set("rows", 10)

      val response = solr.query("elinker", query)
      val solrResult = response.getResults
      if (solrResult.isEmpty) {
        Nil
      } else {
        results.addAll(solrResult.take(maxLinks))
      }
    })

    results.map {
      case document =>
        val resource = document.get("resource").asInstanceOf[String]
        val relevance = 0.0
        (resource, relevance)
    }

  }

  /**
    * Get a list of spotted entity mentions. Spotting is currently done using StanfordNER and custom NER models.
    *
    * @param text Raw text
    * @return Seq of entity annotations and confidence scores
    */
  def getMentions(text: String): Seq[Result] = {
    // Fetch entity mentions in text (only spotting) along with confidence scores
    (for (sentence <- nerClassifier.classify(text)) yield {
      //      println(sentence)
      val p = nerClassifier.documentToDataAndLabels(sentence)
      val cliqueTree: CRFCliqueTree[String] = nerClassifier.getCliqueTree(p)

      var currentBegin = 0
      var currentEnd = 0
      var tokensInCurrentEntity = 0
      var currentClassLabel = "O"
      var currentProbs = 0.0
      var entityMention = ""

      // Iterate through NER-tagged words, join consecutive words into phrases and average their individual confidence scores.
      // Each Result is a single named entity with its position in text and averaged confidence score.
      val entities = (for ((doc, i) <- sentence.zipWithIndex;
                           //mention = doc.get(classOf[CoreAnnotations.TextAnnotation]);
                           begin = doc.get(classOf[CoreAnnotations.CharacterOffsetBeginAnnotation]);
                           end = doc.get(classOf[CoreAnnotations.CharacterOffsetEndAnnotation]);
                           mention = text.substring(begin, end);
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

      if (tokensInCurrentEntity > 0)
        entities += Result(currentClassLabel, entityMention, currentBegin, currentEnd, None, Some(currentProbs / tokensInCurrentEntity))

      entities
    }).flatten.filter(_.mention.nonEmpty)
  }

  /**
    *
    * @param text            Raw text to annotate
    * @param language        Language code, eg. en
    * @param dataset         DatasetMetadata name (eg. dbpedia)
    * @param linksPerMention max. number of links/URIs to fetch for each spotted entity mention
    * @return Seq of entity annotations, URI links and confidence scores
    */
  def getEntities(text: String, language: String, dataset: String, linksPerMention: Int): Seq[Result] = {
    // Spot entities and link to given dataset
    (for (result@Result(entityType, phrase, begin, end, _, Some(score), "") <- getMentions(text)) yield {
      val links = linkToKB(phrase, dataset, language, linksPerMention)
      if (links.isEmpty)
        Seq(result)
      else
        for ((link, _) <- links) yield result.copy(taIdentRef = Some(link))
    }).flatten
  }

  val sparqlProc = new SPARQLProcessor(sparqlEndpoint)

  def getDbpediaTypes(uri: String): Set[String] = sparqlProc.getTypes(uri).toSet

  def receive = {
    case SpotEntities(text, language, outputFormat, prefix, classify, nifVersion) =>
      val results = getMentions(text)

      val nif = new NIFWrapper(prefix, nifVersion, classify)

      nif.context(text)
      nif.entities(results.toList)

      sender ! EnrichedOutput(nif.getNIF(outputFormat))
      stop(self)

    case SpotLinkEntities(text, language, outputFormat, dataset, prefix, numLinks, types, classify, linkingMethod: String, nifVersion: String) =>
      val results = linkingMethod match {
              case similarityFilter.SURFACE_FORM_SIMILARITY => getEntities (text, language, dataset, numLinks).filter(similarityFilter.filterByStringSimilarity(_, dataset, language) )
              case _ => getEntities (text, language, dataset, numLinks)
      }

      val nif = new NIFWrapper(prefix, nifVersion, classify)
      nif.context(text)

      results.foreach {
        case Result(entityType, mention, begin, end, taIdentRef, score, "") =>
          val mentionModel = (taIdentRef, score) match {
            case (Some(ref), Some(s)) if numLinks == 1 =>
              if (types.isEmpty || types.intersect(getDbpediaTypes(ref)).nonEmpty) {
                if (classify) {
                  val otherTypes = getDbpediaTypes(ref).toArray
                  nif.entity(Result.apply(entityType, mention, begin, end, taIdentRef, score), otherTypes)
                } else {
                  nif.entity(Result.apply(entityType, mention, begin, end, taIdentRef, score))
                }
              } else {
                null
              }
            case (Some(ref), Some(s)) =>
              if (types.isEmpty || types.intersect(getDbpediaTypes(ref)).nonEmpty) {
                if (classify) {
                  val otherTypes = getDbpediaTypes(ref).toArray
                  nif.entity(Result.apply(entityType, mention, begin, end, taIdentRef, score), otherTypes)
                } else {
                  nif.entity(Result.apply(null, mention, begin, end, taIdentRef, score))
                }
              } else {
                null
              }
            case (None, Some(score)) =>
              if (classify)
                nif.entity(Result.apply(entityType, mention, begin, end, taIdentRef, Some(score)))
              else
                nif.entity(Result.apply(null, mention, begin, end, taIdentRef, Some(score)))
          }
      }

      // Convert the model to String.
      sender ! EnrichedOutput(nif.getNIF(outputFormat))
      stop(self)

    case LinkEntities(nifString, language, outputFormat, dataset, prefix, numLinks, types, linkingMethod, nifVersion) =>
      val document = parser.getDocumentFromNIFString(nifString)
      val text = document.getText
      val annotations = document.getEntities

      val nif = new NIFWrapper(prefix, nifVersion, false)

      nif.context(text)

      for (annotation <- annotations;
           begin = annotation.getBeginIndex;
           end = annotation.getEndIndex;
           mention = annotation.getMention;
           refs = linkToKB(mention, dataset, language, numLinks)
           if refs.nonEmpty
      ) {

        nif.entity(Result.apply("", annotation.getMention, annotation.getBeginIndex, annotation.getEndIndex, None, None, annotation.getContext))

        for (ref <- refs; uri = ref._1) {
          if (types.isEmpty || types.intersect(getDbpediaTypes(uri)).nonEmpty)
             nif.entity(Result.apply("", mention, begin, end, Option.apply(uri), None))
        }
      }

      // Convert the model to String.
      sender ! EnrichedOutput(nif.getNIF(outputFormat))
      stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => Restart
    }
}


object EntityLinker {

  case class SpotLinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String, numLinks: Int, types: Set[String], classify: Boolean, linkingMethod: String, nifVersion: String) extends RestMessage

  case class SpotEntities(text: String, language: String, outputFormat: String, prefix: String, classify: Boolean, nifVersion: String) extends RestMessage

  case class LinkEntities(text: String, language: String, outputFormat: String, dataset: String, prefix: String, numLinks: Int, types: Set[String], linkingMethod: String, nifVersion: String) extends RestMessage

}

case class Result(entityType: String, mention: String, beginIndex: Int, endIndex: Int, taIdentRef: Option[String], score: Option[Double], context:String = "")