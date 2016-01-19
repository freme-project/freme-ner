package org.elinker.core.api.process

/**
 * Created by nilesh on 15/01/16.
 */
object Rest {
  trait RestMessage

  case class Validation(message: String)
  case class Error(message: String)
  case class StatusCreated(message: AnyRef)
  case class StatusOK(message: AnyRef)
}
