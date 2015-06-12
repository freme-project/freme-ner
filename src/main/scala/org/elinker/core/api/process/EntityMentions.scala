package org.elinker.core.api.process

import akka.actor.Actor
import akka.event.Logging
import spray.routing.RequestContext

import scala.slick.jdbc.{StaticQuery => Q}

/**
* Created by nilesh on 16/12/2014.
*/
object EntityMentions {
  case class Text(text: String, language: String)
}

class EntityMentions(rc: RequestContext) extends Actor {
  import EntityMentions._

  implicit val system = context.system

  val log = Logging(system, getClass)

  def receive = {
    case Text(text, language) =>

  }
}
