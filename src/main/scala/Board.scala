import TeamOps._

import scala.util.Random
import scala.xml.Elem

case object Card {
  val displaySize = 18
}

case class Card(word: String, cardType: CardType, isRevealed: Boolean = false) {

  def shouldDisplay(isCodemaster: Boolean) = isRevealed || isCodemaster

  def tag(isCodemaster: Boolean): Elem = {
    val klass = if (!shouldDisplay(isCodemaster)) "unknown" else cardType.getClass.getSimpleName.toLowerCase.replaceAllLiterally("$", "")
    <div class={s"card $klass"}>{word}</div>
  }


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
    val cardT = Function.tupled(Card(_: String, _: CardType, isRevealed = false))
    val cards = chosenWords.zip(cardTypes).map(cardT)
    Board(cards)
  }

  def toGrid[A](as: Seq[A], perRow: Int): Seq[Seq[A]] = {
    if (as.isEmpty) Nil
    else {
      val (thisRow, rest) = as.splitAt(perRow)
      Seq(thisRow) ++ toGrid(rest, perRow)
    }
  }
}

case class Board(cards: Seq[Card]) {
  val assassinCard = cards.find(_.cardType == Assassin)
  val (redWinCount, blueWinCount) = count()
  val firstTeam: Team = if (redWinCount > blueWinCount) Red else Blue

  def pick(i: Int): (Board, CardType) = {
    val card = cards(i)
    val (newCardsStart, newCardsEnd) = cards.splitAt(i)
    val newCards = (newCardsStart :+ card.copy(isRevealed = true)) ++ newCardsEnd.drop(1)
    (Board(newCards), card.cardType)
  }

  def hasBeenPicked(i: Int): Boolean = cards(i).isRevealed

  def count(countAll: Boolean = true): (Int, Int) = {
    def shouldCount(card: Card) = countAll || card.isRevealed
    def getNumCards(ct: CardType) = cards.count(c => c.cardType == ct && shouldCount(c))

    val redCount = getNumCards(Red)
    val blueCount = getNumCards(Blue)

    (redCount, blueCount)
  }

  val score: (Int, Int) = count(countAll = false)

  val nonAssassinWinner = {
    val (red, blue) = score
    if (red == redWinCount) Some(Red)
    else if (blue == blueWinCount) Some(Blue)
    else None
  }

  val isOver = nonAssassinWinner.isDefined || assassinCard.exists(_.isRevealed)

  def displayGrid(isCodemaster: Boolean) = {
    val grid = Board.toGrid(cards, Math.ceil(Math.sqrt(cards.size)).toInt)
   "\n" +
    grid.map(row => row.map(_.toDisplayString(isCodemaster)).mkString("")).mkString("\n\n\n") +
    "\n"
  }

  def displayList(isCodemaster: Boolean) = {
    "\n" +
    cards.map(_.toDisplayString(isCodemaster).mkString("")).mkString("\n\n") +
    "\n"
  }

}
