package axle.game.cards

import cats.kernel.Order
import cats.Show
import cats.implicits._

object Card {

  def fromString(s: String): IndexedSeq[Card] =
    s.split(",").map(Card(_))

  implicit val orderCard: Order[Card] = (a, b) => Order[Rank].compare(a.rank, b.rank)

  implicit def showCard: Show[Card] = card => "" + card.rank.serialize + card.suit.serialize

  def apply(s: String): Card = Card(Rank(s.charAt(0)), Suit(s.charAt(1)))

}

case class Card(rank: Rank, suit: Suit) {

  def serialize: String = this.show
}
