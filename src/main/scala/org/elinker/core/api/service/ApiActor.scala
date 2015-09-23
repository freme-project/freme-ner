package org.elinker.core.api.service

import akka.actor.Actor


/**
 * Created by nilesh on 3/12/14.
 */
class ApiActor extends Actor with ApiService {
//  val route = tokenRoute ~ apiRoute
  val route = apiRoute

  def actorRefFactory = context

  def receive = runRoute(route)
}
