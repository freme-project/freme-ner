package org.elinker.core.api.service

import akka.actor.{Actor, Props}
import edu.stanford.nlp.ie.crf.CRFClassifier
import eu.freme.common.persistence.repository.DatasetMetadataRepository
import org.elinker.core.api.process.Rest.RestMessage
import org.elinker.core.api.process.{DomainMap, EntityLinker, PerRequestCreator}
import spray.http.StatusCodes._
import spray.http.{MediaType, StatusCode}
import spray.httpx.unmarshalling.{FromStringDeserializer, MalformedContent}
import spray.routing.{HttpService, RequestContext}

/**
 * Exposes functionality of EntityLinker actor. Ultimately mixed into ApiService.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
/*trait EntityApiService extends HttpService with Actor with PerRequestCreator with DomainMap {

  private def entityLinker(message: RestMessage)(implicit requestContext: RequestContext, classifier: CRFClassifier[_]) = perRequest(requestContext, Props(new EntityLinker(classifier, getConfig.solrURI, getConfig.sparqlEndpoint)), message)

  val classifiers = (for((lang, file) <- getConfig.modelFiles)
    yield (lang, CRFClassifier.getClassifierNoExceptions(file))).toMap

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
          parameters("language", "dataset", "format" ? "TTL", "prefix" ? "http://www.freme-project.eu/data/", "numLinks" ? 1, "types" ? "", "domain" ? "", "mode".as(ModeUnmarshaller) ? SpotLinkClassify()) {
            case (language: String, dataset: String, format: String, prefix: String, numLinks: Int, types: String, domain: String, mode: Mode) =>
              entity(as[String]) {
                text =>
                    respondWithMediaType(MediaType.custom(mediaTypes(format))) {
                      implicit requestContext: RequestContext =>
                        implicit val classifier = classifiers(language)

                        val restrictToTypes = {
                          val domainTypes = if (domain.nonEmpty) domains(domain) else Set[String]()
                          val filterTypes = if (types.nonEmpty) types.split(",").toSet else Set[String]()
                          if(domainTypes.isEmpty && filterTypes.isEmpty)
                            domainTypes
                          else if (domainTypes.isEmpty)
                            filterTypes
                          else if (filterTypes.isEmpty)
                            domainTypes
                          else domainTypes.intersect(filterTypes)
                        }

                        // Perform enrichment if the dataset exists
                        // TODO: Should not need dataset param for Spot and SpotClassify. Make it optional for those cases.
                        Option(getDatasetDAO.getRepository.asInstanceOf[DatasetMetadataRepository].findOneByName(dataset)) match {
                          case Some(d) =>
                            mode match {
                              case Spot() =>
                                entityLinker {
                                  EntityLinker.SpotEntities(text, language, format, prefix, classify = false)
                                }
                              case SpotClassify() =>
                                entityLinker {
                                  EntityLinker.SpotEntities(text, language, format, prefix, classify = true)
                                }
                              case Link() =>
                                entityLinker {
                                  EntityLinker.LinkEntities(text, language, format, dataset, prefix, numLinks, restrictToTypes)
                                }
                              case SpotLinkClassify() =>
                                entityLinker {
                                  EntityLinker.SpotLinkEntities(text, language, format, dataset, prefix, numLinks, restrictToTypes, classify = true)
                                }
                              case SpotLink() =>
                                entityLinker {
                                  EntityLinker.SpotLinkEntities(text, language, format, dataset, prefix, numLinks, restrictToTypes, classify = false)
                                }
                            }
                          case None =>
                            complete(BadRequest, "DatasetMetadata does not exist")
                          case _ =>
                            complete(BadRequest)
                        }
                    }
              }
          }
    }

  def complete(status: StatusCode, obj: String)(implicit requestContext: RequestContext) = {
    requestContext.complete(status, obj)
  }
}*/