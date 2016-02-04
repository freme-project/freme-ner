package org.elinker.core.api.process

import org.elinker.core.api.process.Datasets.Dataset
import spray.httpx.SprayJsonSupport
import spray.json._

/**
 * Implicit serializers needed for Spray to serialize outputs (mainly used for Dataset management right now)
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
object JsonImplicits extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DatasetsJsonFormat extends RootJsonFormat[Dataset] with CollectionFormats {

    import spray.json._

    def write(a: Dataset) = a match {
      case Dataset(name, description, totalentities, creationtime) => JsObject(
        "Name" -> JsString(name),
        "Description" -> JsString(description),
        "TotalEntities" -> JsNumber(totalentities),
        "CreationTime" -> JsNumber(creationtime)
      )
    }

    def read(value: JsValue) = ???
  }

  // Probably don't need this anymore. Check implicits being used in EntityLinker etc.
  implicit object MapJsonFormat extends RootJsonFormat[Map[String, Any]] {
    def write(m: Map[String, Any]) = {
      JsObject(m.mapValues {
        case v: String => JsString(v)
        case v: Int => JsNumber(v)
        case v: Map[String, Any] => write(v)
        case v: Any => JsString(v.toString)
      })
    }

    def read(value: JsValue) = ???
  }

}