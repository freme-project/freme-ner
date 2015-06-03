package org.elinker.core.api.auth

import sm.db.DB
import spray.routing.AuthenticationFailedRejection
import spray.routing.authentication.ContextAuthenticator

import scala.concurrent.Future
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Created by nilesh on 15/12/2014.
 */
trait TokenAuthentication extends DB {
  val getUserId = Q.query[(String, Long), String]( """SELECT UserId FROM Token WHERE Token = ? AND Expires > ?""")

  def authenticateToken: ContextAuthenticator[String] = {
    context =>
      Future {
        context.request.uri.query.get("token") match {
          case Some(token) =>
            if(token == "token") // Token override for testing purposes
              Right[AuthenticationFailedRejection, String]("nilesh")
            database withDynSession {
              val userId = getUserId((token, System.currentTimeMillis() / 1000L)).list
              Either.cond(userId.nonEmpty, userId.head, AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, List()))
            }
          case _ =>
            Left[AuthenticationFailedRejection, String](AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, List()))
        }
      }
  }

  //  def authorizeToken
}

