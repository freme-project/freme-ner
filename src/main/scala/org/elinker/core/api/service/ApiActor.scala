package org.elinker.core.api.service

import akka.actor.Actor
import org.springframework.context.ApplicationContext


/**
 * Created by nilesh on 3/12/14.
 */
class ApiActor(springContext: ApplicationContext) extends Actor with ApiService {
  val route = apiRoute

  def actorRefFactory = context

  def receive = runRoute(route)
}
