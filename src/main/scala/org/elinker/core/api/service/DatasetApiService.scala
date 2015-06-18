package org.elinker.core.api.service

import akka.actor.Props
import edu.stanford.nlp.ie.crf.CRFClassifier
import org.elinker.core.api.process.{DatasetActor, EntityLinker}
import spray.routing.{HttpService, RequestContext}

/**
 * Created by nilesh on 03/06/15.
 */
trait DatasetApiService  extends HttpService {
  def datasets(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new DatasetActor(requestContext)))

  def datasetRoute =
    (path("datasets") & get) {
      implicit requestContext: RequestContext =>
        datasets ! DatasetActor.ListDatasets()
    } ~
    path("datasets" / Segment) {
      name =>
        post {
          entity(as[String]) {
            body =>
              parameter("format" ? "TTL") {
                format =>
                  implicit requestContext: RequestContext =>
                    datasets ! DatasetActor.CreateDataset(name, format, body)
              }
          }
        } ~
        get {
          implicit requestContext: RequestContext =>
            datasets ! DatasetActor.ShowDataset(name)
        } ~
        put {
          entity(as[String]) {
            body =>
              parameter("format" ? "TTL") {
                format =>
                  implicit requestContext: RequestContext =>
                    datasets ! DatasetActor.UpdateDataset(name, format, body)
              }
          }
        } ~
        delete {
          implicit requestContext: RequestContext =>
            datasets ! DatasetActor.DeleteDataset(name)
        }
    }
}