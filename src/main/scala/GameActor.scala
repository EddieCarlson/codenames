import GameRequests._
import akka.actor.{Actor, ActorRef}

object GameRequests {
  case class Pick(i: Int)
  case object GetGame
}

class GameActor extends Actor {
  import context.become

  var board: Board = Board.create()

  def receive: Receive = {
    case Pick(i: Int) => {
      val (newBoard, cardType) = board.pick(i)
      if (newBoard.isOver)
      board = newBoard
    }
  }
}

class GameManager extends Actor {

  def receive: Receive = {

  }

}