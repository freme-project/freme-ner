package org.elinker.core.api.db

import scala.slick.driver.JdbcDriver.backend.Database
import scala.slick.jdbc.StaticQuery
import scala.slick.jdbc.meta.MTable


/**
 * Created by nilesh on 15/12/2014.
 */
/*
trait DB {
  val uri: String = ???

  import Database.dynamicSession

  val database = Database.forURL(
    uri,
    driver = "org.sqlite.JDBC")

  implicit class DatabaseOps(database: Database) {
    def apply(sql: String) {
      database withDynSession {
        StaticQuery.updateNA(sql).execute
      }
    }

    def tableNames(): Set[String] = database withDynSession {
      (MTable.getTables.list map {
        _.name.name
      }).toSet
    }
  }

}*/
