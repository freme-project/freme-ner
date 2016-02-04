package org.elinker.core.api.process

import org.elinker.core.api.scala.Config

import scala.io.Source

/**
 * Reads CSV file where the first column is a domain code, followed by rdf:type URIs in the subsequent columns. The domain
 * is defined by the list of type URIs. This is used for naive domain-specific entity linking.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
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
