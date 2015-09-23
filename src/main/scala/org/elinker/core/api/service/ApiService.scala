package org.elinker.core.api.service

import org.elinker.core.api.auth.TokenAuthentication
import org.elinker.evaluation.gerbil.GerbilEvaluationService
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nilesh on 16/12/2014.
 */
trait ApiService extends EntityApiService with DatasetApiService {
  val apiRoute =
    pathPrefix("api") {
          entityRoute ~ datasetRoute
    }
}
