package pe.evolbit.api

import pe.evolbit.actors._
import akka.actor._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import scala.util._
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future

case class User()(implicit system:ActorSystem) {

  import system.dispatcher

  val persistentActor = system.actorOf(Props[UserActionActor], "reply-actor")
  val routes =
    pathSingleSlash  {
      get {
        parameter('message.as[String]) { message =>
          complete {

            persistentActor ! Message(message)
            s"New message has been stored $message. Try to stop your app and open it again to recover your state"
          }
        }
      }
    } ~
    path("state") {
      get {
        complete {
          implicit val timeout = Timeout(5 seconds)
          val currentState = (persistentActor ? "getState").mapTo[State]
          currentState
            .map(x => HttpResponse(entity = s"Your state is ${x.events.head}"))
            .recover { 
              case e:Throwable => {
                print(e)
                HttpResponse(entity = s"There was a problem getting the state...")
              }
            }
        }
      }
    }

}