package org.elinker.core.api.service

import akka.actor.Props
import org.elinker.core.api.process.EntityMentions
import spray.routing.PathMatchers.Segment
import spray.routing.{RequestContext, HttpService}

/**
 * Created by nilesh on 03/06/15.
 */
trait MentionApiService  extends HttpService {
  def mentionActor(implicit requestContext: RequestContext) = actorRefFactory.actorOf(Props(new EntityMentions(requestContext)))

  def mentionRoute(userId: String) =
    path(Segment / "mentions") {
      language =>
        /** Find entity mentions in text */
        (get & parameter("text" ? "")) {
          text =>
            implicit requestContext =>
              mentionActor ! EntityMentions.Text(text, language)
        }

        (post) {
          entity(as[String]) {
            text =>
              implicit requestContext =>
                mentionActor ! EntityMentions.Text(text, language)
          }
        }
    }
}