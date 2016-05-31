package org.elinker.core.api.service

import eu.freme.common.persistence.dao.DatasetMetadataDAO
import org.elinker.core.api.scala.Config
import org.elinker.core.api.java
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Created by nilesh on 16/12/2014.
 */
/*trait ApiService extends EntityApiService {//with DatasetApiService {

  import ApiService._
  def getSpringContext: ApplicationContext = springContext

  override def getDatasetDAO: DatasetMetadataDAO = getSpringContext.getBean("datasetMetadataDAO").asInstanceOf[DatasetMetadataDAO]
  override def getConfig: Config = getSpringContext.getBean("config").asInstanceOf[java.Config].getScalaConfig

  val apiRoute =
    pathPrefix("api") {
          entityRoute //~ datasetRoute
    }
}

object ApiService {
  val springContext: ApplicationContext = new ClassPathXmlApplicationContext("applicationContext.xml")
}*/