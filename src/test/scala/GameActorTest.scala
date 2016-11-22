import GameRequests._
import GameResponses._
import akka.actor._
import org.scalatest.{FunSpec, Matchers}
import akka.pattern.ask
import TeamOps.not

import scala.concurrent._
import duration._

class GameActorTest extends FunSpec with Matchers {

  val sys = ActorSystem("test")
  implicit val timeout: akka.util.Timeout = akka.util.Timeout(2.seconds)

  def test(f: ActorRef => Any) = {
    val gameActor = sys.actorOf(Props[GameActor](), s"gameActor-${java.util.UUID.randomUUID()}")
    f(gameActor)
    gameActor ! PoisonPill
  }

  describe("game") {
    it("should register 1 player") {
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        val gameF = gameActor ? GetGame
        Await.result(gameF, 1.second) match {
          case g: Game => g.players shouldEqual Set(Player("a", Unassigned, isCodemaster = false, isReady = false))
        }
      }
    }
    it("should register 2 players") {
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        gameActor ! RegisterPlayer("b")
        val gameF = gameActor ? GetGame
        Await.result(gameF, 1.second) match {
          case g: Game => g.players shouldEqual
            Set(
              Player("a", Unassigned, isCodemaster = false, isReady = false),
              Player("b", Unassigned, isCodemaster = false, isReady = false)
            )
        }
      }
    }
    it("should remove player") {
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        gameActor ! RegisterPlayer("b")
        gameActor ! UnregisterPlayer("a")
        val gameF = gameActor ? GetGame
        Await.result(gameF, 1.second) match {
          case g: Game => g.players shouldEqual
            Set(
              Player("b", Unassigned, isCodemaster = false, isReady = false)
            )
        }
      }
    }
    it("should not allow player-ready if player has not picked a team") {
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        gameActor ! RegisterPlayer("b")
        gameActor ! SetPlayerReady("b")
        val gameF = gameActor ? GetGame
        Await.result(gameF, 1.second) match {
          case g: Game => g.players shouldEqual
            Set(
              Player("a", Unassigned, isCodemaster = false, isReady = false),
              Player("b", Unassigned, isCodemaster = false, isReady = false)
            )
        }
      }
    }
    it("should allow player-ready if player has picked a team") {
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        gameActor ! RegisterPlayer("b")
        gameActor ! SetPlayerTeam("b", Red)
        gameActor ! SetPlayerReady("b")
        val gameF = gameActor ? GetGame
        Await.result(gameF, 1.second) match {
          case g: Game => g.players shouldEqual
            Set(
              Player("a", Unassigned, isCodemaster = false, isReady = false),
              Player("b", Red, isCodemaster = false, isReady = true)
            )
        }
      }
    }
    it("should play game") {
      case class Teammates(codemaster: String, players: Set[String])
      val blue = Teammates("a", Set("b"))
      val red = Teammates("c", Set("d"))
      test { gameActor =>
        gameActor ! RegisterPlayer("a")
        gameActor ! RegisterCodemaster("a", Blue)
        gameActor ! SetPlayerReady("a")

        gameActor ! RegisterPlayer("b")
        gameActor ! SetPlayerTeam("b", Blue)
        gameActor ! SetPlayerReady("b")

        gameActor ! RegisterPlayer("c")
        gameActor ! RegisterCodemaster("c", Red)
        gameActor ! SetPlayerReady("c")

        gameActor ! RegisterPlayer("d")
        gameActor ! SetPlayerTeam("d", Red)
        gameActor ! SetPlayerReady("d")

        gameActor ? StartGame

        val gameF = gameActor ? GetGame
        val (teamOne, teamTwo) = Await.result(gameF, 1.second) match {
          case g: Game =>
            g.isReady shouldEqual true
            if (g.gamePhase.currentTeam == Blue)
              (blue, red)
            else
              (red, blue)
        }

        gameActor ! GiveClue(teamOne.)
      }
    }
  }
}
