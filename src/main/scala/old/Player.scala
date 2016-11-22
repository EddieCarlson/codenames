package old
/*
import scala.util.Random

object CardType {
  def createRandomCardTypeList = {
    val (redCount, blueCount) = if (Random.nextBoolean()) (9, 8) else (8, 9)
    val reds = makeSeq(Red, redCount)
    val blues = makeSeq(Blue, blueCount)
    val bystanders = makeSeq(Bystander, 7)
    val assassins = Seq(Assassin)
    Random.shuffle(reds ++ blues ++ bystanders ++ assassins)
  }

  def makeSeq(cardType: CardType, num: Int) = {
    1.to(num).map(_ => cardType)
  }
}

sealed trait CardType { val color: String }
sealed trait Team extends CardType
case object Red extends Team { val color = Console.RED }
case object Blue extends Team { val color = Console.BLUE }
case object Unassigned extends Team { val color = Console.YELLOW }
case object Bystander extends CardType { val color = Console.YELLOW }
case object Assassin extends CardType { val color = Console.BLACK }

case class Player(name: String, team: Team, isCodemaster: Boolean = false, isReady: Boolean = false)

object TeamOps {
  implicit def not(team: Team): Team =
    if (team == Blue) Red
    else Blue
}
*/
