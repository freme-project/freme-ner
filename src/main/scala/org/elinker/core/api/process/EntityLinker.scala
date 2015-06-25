package org.elinker.core.api.process

import java.io.StringWriter
import java.util

import akka.actor.Actor
import akka.event.Logging
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.hp.hpl.jena.vocabulary.RDF
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.elinker.serialize.NIFConverter
import spray.routing.RequestContext

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
* Created by nilesh on 16/12/2014.
*/
object EntityLinker {
  case class Text(text: String, language: String, outputFormat: String)
}

class EntityLinker(rc: RequestContext, nerClassifier: CRFClassifier[_]) extends Actor {
  import EntityLinker._

  implicit val system = context.system

  val log = Logging(system, getClass)

  val solr = new HttpSolrClient("http://localhost:8983/solr")

  val jsonMapper = new ObjectMapper() with ScalaObjectMapper
  jsonMapper.registerModule(DefaultScalaModule)
  jsonMapper.registerSubtypes(classOf[Result])

  def linkMention(mention: String): Option[String] = {
    val query = new SolrQuery()
    query.set("q", s"""prefLabel:"$mention"~1^2 OR altLabel:"$mention"~1""")
    val response = solr.query("elinker", query)
    val results = response.getResults
    results.headOption match {
      case Some(topResult) =>
        Some(topResult.get("resource").asInstanceOf[util.ArrayList[String]].head)
      case _ =>
        None
    }
  }

  def receive = {
    case Text(text, language, outputFormat) =>
      val results = new ListBuffer[Result]()
      val triples = nerClassifier.classifyToCharacterOffsets(text)
      for(triple <- triples) {
        val begin = triple.second()
        val end = triple.third()
        val phrase = text.substring(begin, end)
        linkMention(phrase).foreach(resource => results.add(Result(phrase, begin, end, resource)))
      }

      val nif = new NIFConverter
      val contextModel = nif.createContext(text, 0, text.length)
      val contextRes = nif.getContextURI(contextModel)
      results.foreach{
        case Result(mention, begin, end, taIdentRef) =>
          val mentionModel = nif.createMention(mention, begin, end, taIdentRef, contextRes)

          // Merge the context and the mention.
          contextModel.add(mentionModel)
      }
      
      // Convert the model to String.
      val writer = new StringWriter()
      contextModel.write(writer, outputFormat)
      rc.complete(writer.toString)
  }
}

case class Result(mention: String, beginIndex: Int, endIndex: Int, taIdentRef: String)