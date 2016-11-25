import GameRequests._
import akka.actor.{Actor, ActorRef, ActorRefFactory, ActorSystem, Props}
import spray.can._
import akka.io._
import spray.http._
import MediaTypes._
import spray.routing.HttpService
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing.directives.{OnCompleteFutureMagnet, OnSuccessFutureMagnet}
import spray.routing.directives.OnSuccessFutureMagnet._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success
import scala.xml._
import scala.concurrent.ExecutionContext.Implicits.global

object GameRequests {
  case class Pick(i: Int)
  case class GetGame(isCodemaster: Boolean)
}

class GameActor(id: String) extends Actor {
  var board: Board = Board.create()

  def controlTags = board.tags(isCodemaster = false).zipWithIndex.map {
    case (t, i) => <a href={s"/game/$id/pick/$i"}>{t}</a>
  }

  def receive: Receive = {
    case GetGame(false) =>
      sender ! n(controlTags, isGrid = true)
    case GetGame(true) =>
      sender ! n(board.tags(isCodemaster = true), isGrid = true)
    case Pick(i: Int) =>
      val (newBoard, _) = board.pick(i)
      board = newBoard
      sender ! n(controlTags, isGrid = true)
  }

  def n(tags: Seq[Elem], isGrid: Boolean) = {
    val body = <body>{<div class="grid"></div>.copy(child = tags)}</body>
    val head: Elem = <head></head>.copy(child = cssLinks(isGrid))

    val html =
      <html>
        {head}
        {body}
      </html>

    html
  }

  def cssLinks(isGrid: Boolean) = {
    val css = if (isGrid) "gameGrid.css" else "game.css"
    Seq(<link rel="stylesheet" type="text/css" href="/css/cardColors.css"></link>,
      <link rel="stylesheet" type="text/css" href={s"/css/$css"}></link>)
  }
}

class GameManager extends Actor with HttpService {

  def actorRefFactory: ActorRefFactory = context
  implicit val timeout = Timeout(2.seconds)

  def receive = runRoute(route)

  var games = Map.empty[String, ActorRef]

  val route =
    get {
      pathSingleSlash {
        complete(index)
      } ~
      pathPrefix("css") {
        getFromResourceDirectory("css")
      } ~
      path("new") {
        complete(n(newBoardTags(true), isGrid = false))
      } ~
      path("newGrid") {
        complete(n(newBoardTags(true), isGrid = true))
      } ~
      path("newGridPlayer") {
        complete(n(newBoardTags(false), isGrid = true))
      } ~
      path("game" / Segment / "pick" / IntNumber) { case (id, i) =>
        games.get(id) match {
          case Some(ga) => onComplete(actorResponse(ga, Pick(i))) {
            case Success(x: Elem) => complete(x)
          }
          case None => complete(s"no game with id '$id' currently exists")
        }
      } ~
      path("game" / Segment / "codemaster") { id =>
        games.get(id) match {
          case Some(ga) => onComplete(actorResponse(ga, GetGame(true))) {
            case Success(x: Elem) => complete(x)
          }
          case None => complete(s"no game with id '$id' currently exists")
        }
      } ~
      path("game" / Segment) { id =>
        val gameActor = games.get(id).getOrElse {
          val ga = context.actorOf(Props(new GameActor(id)), s"gameActor-$id")
          games = games + (id -> ga)
          ga
        }
        onComplete(actorResponse(gameActor, GetGame(false))) {
          case Success(x: Elem) => complete(x)
        }
      }
    }

  def actorResponse(actor: ActorRef, msg: Any) = OnCompleteFutureMagnet.apply(actor ? msg)

  def newBoardTags(isCodemaster: Boolean): Seq[Elem] = Board.create().tags(isCodemaster)

  def n(tags: Seq[Elem], isGrid: Boolean) = {
    val body = <body>{<div class="grid"></div>.copy(child = tags)}</body>
    val head: Elem = <head></head>.copy(child = cssLinks(isGrid))

    val html =
      <html>
        {head}
        {body}
      </html>

    html
  }

  def cssLinks(isGrid: Boolean) = {
    val css = if (isGrid) "gameGrid.css" else "game.css"
    Seq(<link rel="stylesheet" type="text/css" href="css/cardColors.css"></link>,
      <link rel="stylesheet" type="text/css" href={s"css/$css"}></link>)
    }

  def index =
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="css/game.css"></link>
      </head>
      <body>
        <h1>hi</h1>
        <h3>bye</h3>
      </body>
    </html>
}

object Main extends App {

  implicit val sys = ActorSystem("gameSystem")
  val gm = sys.actorOf(Props[GameManager]())

  IO(Http) ! Http.Bind(gm, interface = "localhost", port = 8080)
}