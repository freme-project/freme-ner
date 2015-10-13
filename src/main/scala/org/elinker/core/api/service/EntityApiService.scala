package org.elinker.core.api.service

import akka.actor.{PoisonPill, Props}
import akka.util.Timeout
import org.elinker.core.api.db.Tables
import spray.httpx.unmarshalling.{MalformedContent, FromStringDeserializer}
import scala.concurrent.duration._
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.{DatasetActor, EntityLinker}
import spray.http.HttpHeaders.`Content-Type`
import spray.http.MediaType
import spray.http.StatusCodes._
import spray.routing.{HttpService, RequestContext}
import spray.http.MediaTypes._

/**
 * Created by nilesh on 03/06/15.
 */
trait EntityApiService  extends HttpService {
  private def entityLinker(implicit requestContext: RequestContext, classifier: CRFClassifier[_]) = actorRefFactory.actorOf(Props(new EntityLinker(classifier)))
  private def datasets(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new DatasetActor(requestContext)))

  val classifiers = Map(("en", CRFClassifier.getClassifierNoExceptions("c:/freme/wikiner-en-ner-model.ser.gz")),
        ("de", CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/german.dewac_175m_600.crf.ser.gz"))
  )


  // List of modes for performing spotting/entity linking
  abstract class Mode()
  case class Spot() extends Mode
  case class Link() extends Mode
  case class SpotLink() extends Mode
  case class SpotLinkClassify() extends Mode
  case class SpotClassify() extends Mode

  val modes = Map(
    Set("spot", "link", "classify") -> SpotLinkClassify(),
    Set("spot", "link") -> SpotLink(),
    Set("spot", "classify") -> SpotClassify(),
    Set("spot") -> Spot(),
    Set("link") -> Link()
  )


  implicit def ModeUnmarshaller = new FromStringDeserializer[Mode] {
    def apply(value: String) =
      try {
        val modeSet = if (value == "all") Set("spot", "link", "classify") else value.split(",").toSet
        Right(modes(modeSet))
      }
      catch {
        case ex: Throwable => Left(MalformedContent(s"Cannot parse: $value", ex))
      }
  }

  val mediaTypes = Map("TTL" -> "text/turtle",
  "TURTLE" -> "text/turtle",
  "N-TRIPLE" -> "application/n-triples",
  "N3" -> "application/n3",
  "RDF/XML" -> "application/rdf+xml",
  "RDF/XML-ABBREV" -> "application/rdf+xml")

  def entityRoute =
    (path("entities") & post) {
          parameters("language", "dataset", "format" ? "TTL", "prefix" ? "http://www.freme-project.eu/data/", "numLinks" ? 1, "mode".as(ModeUnmarshaller) ? SpotLinkClassify()) {
            case (language: String, dataset: String, format: String, prefix: String, numLinks: Int, mode: Mode) =>
              entity(as[String]) {
                text =>
                    respondWithMediaType(MediaType.custom(mediaTypes(format))) {
                      implicit requestContext: RequestContext =>
                        implicit val classifier = classifiers(language)

                        import org.elinker.core.api.process.JsonImplicits._
                        import scala.concurrent.ExecutionContext.Implicits.global
                        import akka.pattern.ask
                        implicit val timeout = Timeout(5 seconds)

                        (datasets ? DatasetActor.GetDataset(dataset)).map{
                          case Some(datasets) =>
                            mode match {
                              case Spot() =>
                                entityLinker ! EntityLinker.SpotEntities(text, language, format, prefix, classify = false)
                              case SpotClassify() =>
                                entityLinker ! EntityLinker.SpotEntities(text, language, format, prefix, classify = true)
                              case Link() =>
                                entityLinker ! EntityLinker.LinkEntities(text, language, format, dataset, prefix)
                              case SpotLinkClassify() =>
                                entityLinker ! EntityLinker.SpotLinkEntities(text, language, format, dataset, prefix, numLinks, classify = true)
                              case SpotLink() =>
                                entityLinker ! EntityLinker.SpotLinkEntities(text, language, format, dataset, prefix, numLinks, classify = false)
                            }
                          case None =>
                            requestContext.complete(BadRequest, Map("Status" -> s"""Dataset with name "$dataset" does not exist."""))
                          case _ =>
                            requestContext.complete(BadRequest)
                        }

                        datasets ! PoisonPill
                    }
              }
          }
    }
}