package org.elinker.core.api.process


import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import eu.freme.common.persistence.dao.DatasetSimpleDAO
import org.elinker.core.api.process.Datasets.{DatasetAlreadyExistsException, DatasetDoesNotExistException}
import org.elinker.core.api.process.PerRequest._
import org.elinker.core.api.process.Rest._
import org.elinker.core.api.scala.Config
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import spray.http.StatusCode

trait PerRequest extends Actor with Json4sSupport {

  import context._
  import JsonImplicits._

  val json4sFormats = DefaultFormats

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(5.seconds)
  target ! message

  def receive = {
    case res: RestMessage => complete(OK, res)
    case StatusCreated(created) => complete(Created, created)
    case StatusOK(ok) => complete(OK, ok)
    case v: Validation    => complete(BadRequest, v)
    case ex: DatasetDoesNotExistException => complete(NotFound, Error("Dataset does not exist"))
    case ex: DatasetAlreadyExistsException => complete(Conflict, Error("Dataset already exists"))
    case ReceiveTimeout   => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
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