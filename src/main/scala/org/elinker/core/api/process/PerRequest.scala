package org.elinker.core.api.process


import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import eu.freme.common.persistence.dao.DatasetSimpleDAO
import org.elinker.core.api.process.Datasets.{Dataset, DatasetAlreadyExistsException, DatasetDoesNotExistException}
import org.elinker.core.api.process.PerRequest._
import org.elinker.core.api.process.Rest._
import org.elinker.core.api.scala.Config
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.json.RootJsonFormat
import spray.routing.RequestContext

import scala.concurrent.duration._

/**
 * Mixed into REST/Service-level actors that need to create per-request actors for business-logic processing.
 *
 * @author Nilesh Chakraborty <nilesh@nileshc.com>
 */
trait PerRequest extends Actor {

  import JsonImplicits._
  import context._

  def r: RequestContext

  def target: ActorRef

  def message: RestMessage

  setReceiveTimeout(10.seconds)
  target ! message

  def receive = {
    case StatusCreated(dataset: Dataset) => complete(Created, dataset)
    case StatusOK(dataset: Dataset) => complete(OK, dataset)
    case StatusOK(datasets: List[Dataset]) => complete(OK, datasets)
    case eo: EnrichedOutput => complete(OK, eo.rdf)
    case ex: DatasetDoesNotExistException => complete(NotFound, "Dataset does not exist")
    case ex: DatasetAlreadyExistsException => complete(Conflict, "Dataset already exists")
    case ReceiveTimeout => complete(GatewayTimeout, "Request timeout")
    case blah => println(blah)
  }

  def complete[T <: AnyRef : RootJsonFormat](status: StatusCode, obj: T) = {
    r.complete(status, obj)
    stop(self)
  }

  def complete(status: StatusCode, obj: String) = {
    r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}

object PerRequest {

  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }

}

trait PerRequestCreator {
  this: Actor =>

  def getDatasetDAO: DatasetSimpleDAO

  def getConfig: Config

  def perRequest(r: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, message)))
}