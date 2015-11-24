package pe.evolbit.actors

import akka.actor._
import akka.persistence._
import akka.event.Logging

case class Message(data: String)
case class Evt(data: String)

case class State(events: List[String] = Nil) {
  def updated(evt: Message): State = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}

class UserActionActor extends PersistentActor {

  override def persistenceId = this.getClass.getName.toLowerCase()

  val log = Logging(context.system, this)

  var state = State()

  def updateState(event: Message): Unit = {
    state = state.updated(event)
  }

  val receiveRecover: Receive = {
    case message: Message => {
      log.info(s"message $message")
      updateState(message)
    }
    case SnapshotOffer(_, snapshot: State) => state = {
      log.info(s"snapshot $snapshot")
      snapshot
    }
  }

  val receiveCommand: Receive = {
    case message:Message =>
      persist(message) { event =>
        updateState(message)
        context.system.eventStream.publish(message)
      }
    case "snap"  => saveSnapshot(state)
    case "getState" =>  sender() ! state
  }
  
}