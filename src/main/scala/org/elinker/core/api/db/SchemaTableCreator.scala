package org.elinker.core.api.db



import scala.slick.codegen.SourceCodeGenerator
import scala.slick.driver.SQLiteDriver
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.createModel

/**
 * Created by nilesh on 15/12/2014.
 */
/*
object SchemaTableCreator {
  def main(args: Array[String]): Unit = {
    val db = Database.forURL("jdbc:sqlite:/Users/nilesh/IdeaProjects/elinker/elinker.db", driver = "org.sqlite.JDBC")

    // fetch data model
    val model = db.withSession { implicit session =>
      createModel(SQLiteDriver.getTables.list, SQLiteDriver) // you can filter specific tables here
    }
    // customize code generator
    val codegen = new SourceCodeGenerator(model) {
      // override mapped table and class name
      override def entityName =
        dbTableName => dbTableName.toLowerCase

      override def tableName =
        dbTableName => dbTableName.toLowerCase.toCamelCase

      // add some custom import
      //      override def code = "import foo.{MyCustomType,MyCustomTypeMapper}" + "\n" + super.code

      // override table generator
      override def Table = new Table(_) {
        // disable entity class generation and mapping
//        override def TableClass = new TableClass {
//          override def enabled = false
//        }
//
//        override def TableValue = new TableValue {
//          override def enabled = false
//        }

        // override contained column generator
        //        override def Column = new Column(_){
        //          // use the data model member of this column to change the Scala type,
        //          // e.g. to a custom enum or anything else
        //          override def rawType =
        //            if(model.name == "SOME_SPECIAL_COLUMN_NAME") "MyCustomType" else super.rawType
        //        }
      }
    }
    codegen.writeToFile(
      "scala.slick.driver.SQLiteDriver", "/Users/nilesh/IdeaProjects/elinker/src/main/scala", "org.elinker.core.api.db", "Tables", "Tables.scala"
    )
  }
}
*/
//SchemaTableCreator.main(Array())