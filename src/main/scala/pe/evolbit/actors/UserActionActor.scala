package pe.evolbit.actors

import akka.actor._
import akka.persistence._
import akka.event.Logging

case class AddFund(amount: Int)

trait FundEvent {
  def amount:Int
}
case class AddFundEvent(amount:Int) extends FundEvent

case class FundState(totalBalance:Int = 0) {
  def updateBalance(amount:Int) = copy(totalBalance + amount)
}

class UserActionActor extends PersistentActor {

  override def persistenceId = this.getClass.getName.toLowerCase()

  val log = Logging(context.system, this)

  var state = FundState()
  var messagesCounter = 0

  def updateState(event: FundEvent): Unit = {
    val amount = event match {
      case AddFundEvent(amount) => event.amount
    }
    log.info(s"current balance: ${state.totalBalance}")
    log.info(s"new amount: ${amount}")
    log.info(s"new balance: ${state.totalBalance + amount}")
    state = state.updateBalance(amount)

    messagesCounter = messagesCounter + 1
      if(messagesCounter > 5){
        saveSnapshot(state)
        messagesCounter = 0
    }
  }

  val receiveRecover: Receive = {
    case AddFundEvent(amount) =>
      val addFundEvent = AddFundEvent(amount)
      log.info(s"recover addFundEvent $addFundEvent")
      updateState(addFundEvent)

    case SnapshotOffer(_, snapshot: FundState) => 
      log.info(s"snapshot $snapshot")
      state = snapshot
  }

  val receiveCommand: Receive = {
    case AddFund(amount) =>
      val addFundEvent = AddFundEvent(amount)
      persist(addFundEvent) { event =>
        updateState(addFundEvent)
      }
      context.system.eventStream.publish(addFundEvent)

    case "getState" =>  sender() ! state
  }
  
}