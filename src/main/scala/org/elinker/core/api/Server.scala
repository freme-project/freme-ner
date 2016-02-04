package org.elinker.core.api


import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import org.elinker.core.api.service.ApiActor
import org.springframework.context.support.ClassPathXmlApplicationContext
import spray.can.Http

/**
 * Main class to start up Spray-based REST API server.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
object Server {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("api-service")
    val log = Logging(system, getClass)

    val springContext = new ClassPathXmlApplicationContext("applicationContext.xml")

    // create and start our service actor
    val service = system.actorOf(Props(new ApiActor(springContext)), "api")

    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
  }
}
