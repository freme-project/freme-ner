package org.elinker.core.api.service

import akka.actor.{Actor, Props}
import eu.freme.common.persistence.dao.DatasetSimpleDAO
import org.elinker.core.api.process.Rest.RestMessage
import org.elinker.core.api.process.{PerRequestCreator, Datasets, EntityLinker}
import org.elinker.core.api.scala.Config
import org.springframework.context.ApplicationContext
import spray.routing.{HttpService, RequestContext}

/**
 * Created by nilesh on 03/06/15.
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
                    println("SPARQLing")
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
                      println("Normaling")
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
              Datasets.ShowDataset(name)
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