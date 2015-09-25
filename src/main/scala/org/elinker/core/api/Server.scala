package org.elinker.core.api

import java.util.Properties

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import org.elinker.core.api.service.ApiActor
import spray.can.Http

/**
 * Created by nilesh on 02/06/15.
 */
object Server {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("api-service")
    val log = Logging(system, getClass)

    // create and start our service actor
    val service = system.actorOf(Props[ApiActor], "api")

    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
  }
}
