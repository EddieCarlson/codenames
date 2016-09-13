

case class Card(word: String, cardType: CardType, var isRevealed: Boolean)

trait Board {

  val grid: Seq[Seq[Card]]

  def pick(x: Int, y: Int): CardType = {
    val card = grid(x)(y)
    card.isRevealed = true
    card.cardType
  }

  def score(team: Team) = grid.foldLeft(0) { case (acc, row) =>
    acc + row.foldLeft(0) { case (rowAcc, card) =>
      if (card.cardType == team && card.isRevealed) rowAcc + 1
      else rowAcc
    }
  }

}
