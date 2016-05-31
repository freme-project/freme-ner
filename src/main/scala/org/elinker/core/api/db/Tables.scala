package org.elinker.core.api.db
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
//object Tables extends {
//  val profile = scala.slick.driver.SQLiteDriver
//} with Tables
//
///** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
//trait Tables {
//  val profile: scala.slick.driver.JdbcProfile
//  import profile.simple._
//  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
//  import scala.slick.jdbc.{GetResult => GR}
//
//  /** DDL for all tables. Call .create to execute. */
//  lazy val ddl = DatasetsQ.ddl
//
//  /** Entity class storing rows of table Datasets
//   *  @param name Database column Name DBType(VARCHAR)
//   *  @param totalentities Database column TotalEntities DBType(INTEGER)
//   *  @param creationtime Database column CreationTime DBType(DATE) */
//  case class datasets(name: Option[String], description: Option[String], totalentities: Option[Int], creationtime: Option[java.sql.Timestamp])
//  /** GetResult implicit for fetching datasets objects using plain SQL queries */
//  implicit def GetResultdatasets(implicit e0: GR[Option[String]], e1: GR[Option[Int]], e2: GR[Option[java.sql.Timestamp]]): GR[datasets] = GR{
//    prs => import prs._
//    datasets.tupled((<<?[String], <<?[String], <<?[Int], <<?[java.sql.Timestamp]))
//  }
//  /** Table description of table Datasets. Objects of this class serve as prototypes for rows in queries. */
//  class Datasets(_tableTag: Tag) extends Table[datasets](_tableTag, "Datasets") {
//    def * = (name, description, totalentities, creationtime) <> (datasets.tupled, datasets.unapply)
//
//    /** Database column Name DBType(VARCHAR) */
//    val name: Column[Option[String]] = column[Option[String]]("Name")
//    /** Database column Description DBType(VARCHAR) */
//    val description: Column[Option[String]] = column[Option[String]]("Description")
//    /** Database column TotalEntities DBType(INTEGER) */
//    val totalentities: Column[Option[Int]] = column[Option[Int]]("TotalEntities")
//    /** Database column CreationTime DBType(DATE) */
//    val creationtime: Column[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("CreationTime")
//  }
//  /** Collection-like TableQuery object for table Datasets */
//  lazy val DatasetsQ = new TableQuery(tag => new Datasets(tag))
//}