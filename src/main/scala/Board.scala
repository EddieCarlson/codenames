

case class Card(word: String, cardType: CardType, var revealedBy: Option[Team])

trait Board {

  val grid: Seq[Seq[Card]]
  val (redWinCount, blueWinCount) = count()

  def pick(x: Int, y: Int, team: Team): CardType = {
    val card = grid(x)(y)
    card.revealedBy = Some(team)
    card.cardType
  }

  def count(countAll: Boolean = true): (Int, Int) = {
    def shouldCount(card: Card) = countAll || card.revealedBy.isDefined

    grid.foldLeft((0, 0)) { case ((red, blue), row) =>
      val (r, b) = row.foldLeft((0, 0)) { case ((redRow, blueRow), card) =>
        if (card.cardType == Red && shouldCount(card)) (redRow + 1, blueRow)
        else if (shouldCount(card)) (redRow, blueRow + 1)
        else (redRow, blueRow)
      }
      (red + r, blue + b)
    }
  }

  def score: (Int, Int) = count(countAll = false)

  def winner: Option[Team] = {
    val (red, blue) = score
    if (red == redWinCount) Some(Red)
    else if (blue == blueWinCount) Some(Blue)
    else None
  }

}
