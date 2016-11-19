import GameRequests._
import GameResponses._
import akka.actor.{Actor, ActorRef}

object GameRequests {
  case class Pick(id: String, x: Int, y: Int)
  case class GiveClue(numWords: Int)
  case class RegisterPlayer(id: String)
  case class UnregisterPlayer(id: String)
  case class RegisterCodemaster(name: String, team: Team)
  case class UnregisterCodemaster(name: String)
  case class SetPlayerTeam(name: String, team: Team)
  case class SetPlayerName(oldName: String, newName: String)
  case class SetPlayerReady(name: String, ready: Boolean = true)
  case object StartGame
}

object GameResponses {
  case object PlayerIsCodemaster
  case class CannotRegisterPlayer(reason: String)
  case class PlayerCannotBeCodemaster(reason: String)
  case class UnknownPlayer(name: String)
  case object PlayerUnregistered
}

class GameActor extends Actor {
  import context.become

  var game: Game = Game.create

  def receive: Receive = registerPlayers orElse registerCodemasters orElse {
    case StartGame => ???
  }

  def registerCodemasters: Receive = {
    case RegisterCodemaster(name, team) =>
      sendToPlayer(name, p => {
        if (game.codemasters.map(_.team).contains(team)) {
          PlayerCannotBeCodemaster(s"there is already a $team codemaster, cannot have 2 $team codemasters." +
            s"if you wish to become codemaster, ask the current $team codemaster to step down.")
        } else {
          game = game.addPlayer(Player(name, team, isCodemaster = true, isReady = false))
          PlayerIsCodemaster
        }
      })
    case UnregisterCodemaster(name) =>
      sendToPlayer(name, p => {
        game.addPlayer(p.copy(isCodemaster = false, isReady = false)); ()
      })
  }

  def sendToPlayer(name: String, f: Player => Any) = {
    game.getPlayer(name) match {
      case Some(p) => sender ! f(p)
      case None => sender ! UnknownPlayer(name)
    }
  }

  def playerPlay: Receive = {
    case Pick(name, x, y) if game.board.hasBeenPicked(x, y) =>
    case Pick(name, x, y) =>
      val teamBefore = game.gamePhase.currentTeam
      game = game.pick(name, x, y)
      if (teamBefore != game.gamePhase.currentTeam) become(codemasterPlay)
  }

  def codemasterPlay: Receive = {
    case GiveClue(numWords) => game.gamePhase match {
      case phase: CodemasterPhase => phase.becomePlayerPhase(numWords)
      case _ =>
    }
  }

  def registerPlayers: Receive = {
    case RegisterPlayer(name) if game.getPlayer(name).isDefined =>
      sender ! CannotRegisterPlayer(s"player with name '$name' already exists")
    case RegisterPlayer(name) =>
      val player = Player(name, Bystander)
      game = game.addPlayer(player)
      sender ! player
    case UnregisterPlayer(name) =>
      game = game.removePlayer(name)
      sender ! PlayerUnregistered
    case SetPlayerTeam(name, team) =>
      sendToPlayer(name, p => {
        val newP = p.copy(team = team, isReady = false)
        game = game.addPlayer(newP)
        sender ! newP
      })
    case SetPlayerName(oldName, newName) =>
      sendToPlayer(oldName, p => {
        val newP = p.copy(name = newName, isReady = false)
        game = game.addPlayer(newP)
        sender ! newP
      })
    case SetPlayerReady(name, ready) =>
      sendToPlayer(name, p => {
        val newP = p.copy(isReady = ready)
        game = game.addPlayer(newP)
        sender ! newP
      })
  }
}