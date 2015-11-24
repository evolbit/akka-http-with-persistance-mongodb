package pe.evolbit

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import pe.evolbit.api._

object ApplicationMain extends App {
  implicit val system = ActorSystem("ActorSystem")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val user = User()

  val bindingFuture = Http().bindAndHandle(user.routes, "0.0.0.0", 8080)
}