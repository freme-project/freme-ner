package org.elinker.core.api.service

import org.elinker.core.api.auth.TokenAuthentication
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nilesh on 16/12/2014.
 */
trait ApiService extends MentionApiService {
  val apiRoute =
    pathPrefix("api") {
          mentionRoute
    }
}
