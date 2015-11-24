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

case class Funds()(implicit system:ActorSystem) {

  import system.dispatcher

  val persistentActor = system.actorOf(Props[FundActor], "reply-actor")
  val routes =
    pathSingleSlash  {
      get {
        complete{
          s"Akka http"
        }
      }
    } ~
    path("add-funds" / IntNumber)  { fund =>
      get {
          complete {
            persistentActor ! AddFund(fund)
            s"""New fund has been added $fund. 
            |Try to stop your app and open it again to recover your balance""".stripMargin
          }
        }
    } ~
    path("current-balance") {
      get {
        complete {
          implicit val timeout = Timeout(5 seconds)
          val currentState = (persistentActor ? "getState").mapTo[FundState]
          currentState
            .map{ x => 
              val currentBalance = x.totalBalance
              HttpResponse(entity = s"Your balance is ${currentBalance}")
            }
            .recover { 
              case e:Throwable => {
                HttpResponse(entity = s"There was a problem getting the balance... $e")
              }
            }
        }
      }
    }

}