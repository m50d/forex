package forex.domain

import cats.instances.either._
import cats.syntax.apply._

case class Rate(pair: RatePair, price: Price, timestamp: Timestamp)

final case class RatePair(from: Currency, to: Currency) {
  def asSymbol = from.toString + to.toString
}

object RatePair {
  def fromSymbol(s: String) = s.grouped(3).toSeq match {
    case Seq(from, to) => (Currency.fromString(from), Currency.fromString(to)).mapN(RatePair.apply)
  }
}
