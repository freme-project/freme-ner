package org.elinker.core.api.service

import eu.freme.common.persistence.dao.DatasetSimpleDAO
import org.elinker.core.api.auth.TokenAuthentication
import org.elinker.core.api.scala.Config
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by nilesh on 16/12/2014.
 */
trait ApiService extends EntityApiService with DatasetApiService {

  def getSpringContext: ApplicationContext = new ClassPathXmlApplicationContext("applicationContext.xml")

  override def getDatasetDAO: DatasetSimpleDAO = getSpringContext.getBean("datasetSimpleDAO").asInstanceOf[DatasetSimpleDAO]
  override def getConfig: Config = getSpringContext.getBean("config").asInstanceOf[Config]

  val apiRoute =
    pathPrefix("api") {
          entityRoute ~ datasetRoute
    }
}
