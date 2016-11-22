import GameRequests._
import GameResponses._
import akka.actor.{Actor, ActorRef}

object GameRequests {
  case class Pick(name: String, x: Int, y: Int)
  case class GiveClue(name: String, numWords: Int)
  case class RegisterPlayer(name: String)
  case class UnregisterPlayer(name: String)
  case class RegisterCodemaster(name: String, team: Team)
  case class UnregisterCodemaster(name: String)
  case class SetPlayerTeam(name: String, team: Team)
  case class SetPlayerName(oldName: String, newName: String)
  case class SetPlayerReady(name: String, ready: Boolean = true)
  case object StartGame
  case object GetGame
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
    case GetGame => sender ! game
    case StartGame => become(codemasterPlay)
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
    case GiveClue(name, numWords) =>
      (game.getPlayer(name), game.gamePhase) match {
        case (Some(Player(_, team, true, _)), phase: CodemasterPhase) if team == phase.currentTeam =>
          phase.becomePlayerPhase(numWords)
        case _ =>
      }
  }

  def registerPlayers: Receive = {
    case RegisterPlayer(name) if game.getPlayer(name).isDefined =>
      sender ! CannotRegisterPlayer(s"player with name '$name' already exists")
    case RegisterPlayer(name) =>
      val player = Player(name, Unassigned)
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
      sendToPlayer(name, p => { // player must have assigned team to be ready
        if (p.team == Unassigned) sender ! p
        else {
          val newP = p.copy(isReady = ready)
          game = game.addPlayer(newP)
          sender ! newP
        }
      })
  }
}