

sealed trait CardType
sealed trait Team extends CardType
case object Red extends Team
case object Blue extends Team
case object Bystander extends CardType
case object Assassin extends CardType

case class Player(id: String, team: Team)
