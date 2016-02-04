package org.elinker.core.api.process

/**
 * High-level REST messages.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 * @todo Maybe add more messages here from other actor classes?
 * @todo We should ideally seperate REST messages from general entity linking API-level messages.
 */
object Rest {
  trait RestMessage

  case class Validation(message: String)
  case class Error(message: String)
  case class StatusCreated(message: AnyRef)
  case class StatusOK(message: AnyRef)
  case class EnrichedOutput(rdf: String)
}
