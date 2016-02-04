package org.elinker.core.api.service

import akka.actor.{Actor, Props}
import org.elinker.core.api.process.Rest.RestMessage
import org.elinker.core.api.process.{Datasets, PerRequestCreator}
import spray.routing.{HttpService, RequestContext}

/**
 * Exposes functionality of Datasets actor. Ultimately mixed into ApiService.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
trait DatasetApiService  extends HttpService with Actor with PerRequestCreator {
  def datasets(message: RestMessage)(implicit requestContext: RequestContext) = perRequest(requestContext, Props(new Datasets(getConfig.solrURI, getDatasetDAO)), message)
  import Datasets._

  def datasetRoute =
    path("datasets") {
      pathEndOrSingleSlash {
        get {
          implicit requestContext: RequestContext =>
            datasets {
              Datasets.ListDatasets()
            }
        } ~
        post {
          entity(as[String]) {
            body =>
              parameter("name", "description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "", "sparql", "endpoint") {
                (name: String, description: String, format: String, language: String, properties: String, query: String, endpoint: String) =>
                  implicit requestContext: RequestContext =>
                    datasets {
                      Datasets.CreateDataset(name, description, format, SparqlInput(query, endpoint), language, properties.split(",").filterNot(_.isEmpty))
                    }
              } ~
                parameter("name", "description"? "", "format" ? "TTL", "url", "language" ? "xx", "properties" ? "") {
                  (name: String, description: String, format: String, url: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets {
                        Datasets.CreateDataset(name, description, format, UrlInput(url), language, properties.split(",").filterNot(_.isEmpty))
                      }
                } ~
                parameter("name", "description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "") {
                  (name: String, description: String, format: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets {
                        Datasets.CreateDataset(name, description, format, TextInput(body), language, properties.split(",").filterNot(_.isEmpty))
                      }
                }
          }
        }
      }
    } ~
    path("datasets" / Segment) {
      name =>
        get {
          implicit requestContext: RequestContext =>
            datasets {
              Datasets.GetDataset(name)
            }
        } ~
        put {
          entity(as[String]) {
            body =>
              parameter("description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "", "sparql", "endpoint") {
                (description: String, format: String, language: String, properties: String, query: String, endpoint: String) =>
                  implicit requestContext: RequestContext =>
                    datasets {
                      Datasets.UpdateDataset(name, description, format, SparqlInput(query, endpoint), language, properties.split(",").filterNot(_.isEmpty))
                    }
              } ~
                parameter("description"? "", "format" ? "TTL", "url", "language" ? "xx", "properties" ? "") {
                  (description: String, format: String, url: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets {
                        Datasets.UpdateDataset(name, description, format, UrlInput(url), language, properties.split(",").filterNot(_.isEmpty))
                      }
                } ~
                parameter("description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "") {
                  (description: String, format: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets {
                        Datasets.UpdateDataset(name, description, format, TextInput(body), language, properties.split(",").filterNot(_.isEmpty))
                      }
                }
          }
        } ~
        delete {
          implicit requestContext: RequestContext =>
            datasets {
              Datasets.DeleteDataset(name)
            }
        }
    }
}