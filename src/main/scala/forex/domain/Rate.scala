package forex.domain

import cats.instances.either._
import cats.syntax.apply._

case class Rate(pair: RatePair, price: Price, timestamp: Timestamp)

final case class RatePair(from: Currency, to: Currency) {
  def asSymbol = to.toString + from.toString
}

object RatePair {
  def fromSymbol(s: String) = s.grouped(3).toSeq match {
    case Seq(to, from) => (Currency.fromString(from), Currency.fromString(to)).mapN(RatePair.apply)
    case _ => Left(s"Unknown symbol $s")
  }

  val all = for {
    from ← Currency.all
    to ← Currency.all
    if (from != to)
  } yield apply(from, to)
}
