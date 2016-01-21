package org.elinker.core.api.process

import org.elinker.core.api.scala.Config

import scala.io.Source

/**
 * Created by nilesh on 21/01/16.
 */
trait DomainMap {
  def getConfig: Config

  val domains: Map[String, Set[String]] = {
    try {
      Source.fromFile(getConfig.domainsFile).getLines().map {
        case line =>
          val split = line.split(",").filterNot(_.isEmpty)
          (split(0), split.drop(1).toSet)
      }.toMap
    } catch {
      case ex: Exception =>
        Map()
    }
  }
}
