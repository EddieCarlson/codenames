package old
/*
import old.TeamOps._

import scala.util.Random

case object Card {
  val displaySize = 18
}

case class Card(word: String, cardType: CardType, var revealedBy: Option[Team] = None) {
  def isRevealed = revealedBy.isDefined

  def format(coloredWord: String) = {
    val length = word.length
    val diff = Card.displaySize - length
    val pad = " "  * (diff / 2)
    val startPad = pad
    val endPad = if (diff % 2 == 0) pad else pad + " "
    s"$startPad$coloredWord$endPad"
  }

  def toDisplayString(isCodemaster: Boolean) = {
    format(toColorString(isCodemaster))
  }

  private def toColorString(isCodemaster: Boolean) = {
    if (isCodemaster || isRevealed) s"${cardType.color}$word${Console.RESET}"
    else word
  }
}

object Board {

  def create(words: Seq[String] = Game.words): Board = {
    val chosenWords = Random.shuffle(words).take(25)
    val cardTypes = CardType.createRandomCardTypeList
    val cards = chosenWords.zip(cardTypes).map {
      case (word, cardType) => Card(word, cardType)
    }
    new Board {
      val grid = toGrid(cards, 5)
    }
  }

  def toGrid[A](as: Seq[A], perRow: Int): Seq[Seq[A]] = {
    if (as.isEmpty) Nil
    else {
      val (thisRow, rest) = as.splitAt(perRow)
      Seq(thisRow) ++ toGrid(rest, perRow)
    }
  }
}

trait Board {

  val grid: Seq[Seq[Card]]
  lazy val flatGrid = grid.flatten
  lazy val assassinCard = flatGrid.find(_.cardType == Assassin)
  lazy val (redWinCount, blueWinCount) = count()
  lazy val firstTeam: Team = if (redWinCount > blueWinCount) Red else Blue

  def pick(x: Int, y: Int, team: Team): CardType = {
    val card = grid(x)(y)
    card.revealedBy = Some(team)
    card.cardType
  }

  def hasBeenPicked(x: Int, y: Int): Boolean = grid(x)(y).revealedBy.isDefined

  def count(countAll: Boolean = true): (Int, Int) = {
    def shouldCount(card: Card) = countAll || card.isRevealed

    flatGrid.foldLeft((0, 0)) { case ((red, blue), card) =>
      if (card.cardType == Red && shouldCount(card)) (red + 1, blue)
      else if (shouldCount(card)) (red, blue + 1)
      else (red, blue)
    }
  }

  def score: (Int, Int) = count(countAll = false)

  def winner: Option[Team] = {
    assassinCard.flatMap(_.revealedBy.map(not).orElse(nonAssassinWinner))
  }

  def nonAssassinWinner = {
    val (red, blue) = score
    if (red == redWinCount) Some(Red)
    else if (blue == blueWinCount) Some(Blue)
    else None
  }

  def display(isCodemaster: Boolean) = {
   "\n" +
    grid.map(row => row.map(_.toDisplayString(isCodemaster)).mkString("")).mkString("\n\n\n") +
    "\n"
  }

}
*/
