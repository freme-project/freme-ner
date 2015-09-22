package org.elinker.core.api.service

import akka.actor.Props
import org.elinker.core.api.process.{DatasetActor, EntityLinker}
import spray.routing.{HttpService, RequestContext}

/**
 * Created by nilesh on 03/06/15.
 */
trait DatasetApiService  extends HttpService {
  def datasets(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new DatasetActor(requestContext)))
//  def datasets(dataset: String)(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new DatasetActor(requestContext, dataset)))
  import DatasetActor._

  def datasetRoute =
    path("datasets") {
      pathEndOrSingleSlash {
        get {
          implicit requestContext: RequestContext =>
            datasets ! DatasetActor.ListDatasets()
        } ~
        post {
          entity(as[String]) {
            body =>
              parameter("name", "description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "", "sparql", "endpoint") {
                (name: String, description: String, format: String, language: String, properties: String, query: String, endpoint: String) =>
                  implicit requestContext: RequestContext =>
                    println("SPARQLing")
                    datasets ! DatasetActor.CreateDataset(name, description, format, SparqlInput(query, endpoint), language, properties.split(",").filterNot(_.isEmpty))
              } ~
                parameter("name", "description"? "", "format" ? "TTL", "url", "language" ? "xx", "properties" ? "") {
                  (name: String, description: String, format: String, url: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets ! DatasetActor.CreateDataset(name, description, format, UrlInput(url), language, properties.split(",").filterNot(_.isEmpty))
                } ~
                parameter("name", "description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "") {
                  (name: String, description: String, format: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      println("Normaling")
                      datasets ! DatasetActor.CreateDataset(name, description, format, TextInput(body), language, properties.split(",").filterNot(_.isEmpty))
                }
          }
        }
      }
    } ~
    path("datasets" / Segment) {
      name =>
        get {
          implicit requestContext: RequestContext =>
            datasets ! DatasetActor.ShowDataset(name)
        } ~
        put {
          entity(as[String]) {
            body =>
              parameter("description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "", "sparql", "endpoint") {
                (description: String, format: String, language: String, properties: String, query: String, endpoint: String) =>
                  implicit requestContext: RequestContext =>
                    datasets ! DatasetActor.UpdateDataset(name, description, format, SparqlInput(query, endpoint), language, properties.split(",").filterNot(_.isEmpty))
              } ~
                parameter("description"? "", "format" ? "TTL", "url", "language" ? "xx", "properties" ? "") {
                  (description: String, format: String, url: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets ! DatasetActor.UpdateDataset(name, description, format, UrlInput(url), language, properties.split(",").filterNot(_.isEmpty))
                } ~
                parameter("description"? "", "format" ? "TTL", "language" ? "xx", "properties" ? "") {
                  (description: String, format: String, language: String, properties: String) =>
                    implicit requestContext: RequestContext =>
                      datasets ! DatasetActor.UpdateDataset(name, description, format, TextInput(body), language, properties.split(",").filterNot(_.isEmpty))
                }
          }
        } ~
        delete {
          implicit requestContext: RequestContext =>
            datasets ! DatasetActor.DeleteDataset(name)
        }
    }
}