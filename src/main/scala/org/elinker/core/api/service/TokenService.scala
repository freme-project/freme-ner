//package org.elinker.core.api.service
//
//import org.elinker.core.api.auth.UserAuthentication
//import spray.routing.HttpService
//import spray.routing.authentication._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//
///**
// * Created by nilesh on 9/12/14.
// */
//trait TokenService extends HttpService with UserAuthentication {
//  val tokenRoute =
//    path("token") {
//      authenticate(BasicAuth(authenticateUser, "token")) {
//        user => {
//          complete {
//            user.token
//          }
//        }
//      }
//    }
//}
