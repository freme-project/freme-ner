package org.elinker.core.api.auth

import org.elinker.core.api.db.DB
import spray.routing.authentication._

import scala.concurrent.Future
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.jdbc.{StaticQuery => Q}

/**
 * Created by nilesh on 15/12/2014.
 */
trait UserAuthentication extends DB {
  val userAuth = Q.query[(String, String), (String, String)]( """SELECT * FROM User WHERE UserId = ? AND Passord = ?""")
  val getToken = Q.query[(String, Long), String]( """SELECT Token FROM Token WHERE UserId = ? AND Expires > ?""")
  val newToken = Q.update[(String, String, Long)]( """INSERT OR REPLACE INTO Token VALUES (?, ?, ?)""")

  def authenticateUser: UserPassAuthenticator[User] = {
    case Some(UserPass(user, pass)) =>
      Future {
        database withDynSession {
          if (userAuth((user, pass)).list.nonEmpty) {
            val tokenFromDB = getToken((user, System.currentTimeMillis() / 1000L)).list
            val token =
              if (tokenFromDB.nonEmpty) {
                tokenFromDB.head
              } else {
                val token = java.util.UUID.randomUUID.toString.replaceAll("-", "")
                newToken((user, token, System.currentTimeMillis() / 1000L + 45 * 60)).execute
                token
              }
            Some(User(userId = user, token = token))
          }
          else
            None
        }
      }
  }

  case class User(userId: String, token: String) {}
}