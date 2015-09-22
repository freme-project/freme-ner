package org.elinker.core.api.process

import akka.actor.Actor
import akka.event.Logging
import org.elinker.core.api.db.DB
import sm.db.DB
import sm.db.Tables.documentfolder
import spray.http.{StatusCode, StatusCodes}
import spray.routing.RequestContext

import scala.concurrent._
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import sm.db.ApiJsonProtocol._
import spray.json._
import scala.slick.jdbc.{StaticQuery => Q, Invoker}

/**
* Created by nilesh on 16/12/2014.
*/
object EntityMentions {
  case class Text(text: String, language: String)
}

class EntityMentions(rc: RequestContext) extends Actor {
  import EntityMentions._

  implicit val system = context.system

  import system.dispatcher

  val log = Logging(system, getClass)

  def receive = {
    case Text(text, language) =>

  }
}
