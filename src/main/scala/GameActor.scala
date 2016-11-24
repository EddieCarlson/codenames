import GameRequests._
import akka.actor.{Actor, ActorRef, ActorRefFactory, ActorSystem, Props}
import spray.can._
import akka.io._
import spray.http._
import MediaTypes._
import spray.routing.HttpService

import scala.xml.{Elem, Node}

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
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(HttpMethods.GET, Uri.Path("/new"), _, _, _) =>
      sender ! HttpResponse(entity = HttpEntity(`text/html`, <h1>hi</h1>.toString))

//    case HttpRequest(HttpMethods.GET, Uri.Path("/css"), _, _, _) =>
//      sender ! HttpResponse(entity = HttpEntity(`text/html`, css))
  }

}

class GameManagerR extends Actor with HttpService {

  def actorRefFactory: ActorRefFactory = context

  def receive = runRoute(route)

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
      }
    }

  def newBoardTags(isCodemaster: Boolean): Seq[Elem] = Board.create().cards.map(_.tag(isCodemaster))

  def n(tags: Seq[Elem], isGrid: Boolean) = {
    val body = <body>{<div class="grid"></div>.copy(child = tags)}</body>
    val css = if (isGrid) "css/gameGrid.css" else "css/game.css"
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href={css}></link>
      </head>
      {body}
    </html>
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
  val gm = sys.actorOf(Props[GameManagerR]())

  IO(Http) ! Http.Bind(gm, interface = "localhost", port = 8080)
}