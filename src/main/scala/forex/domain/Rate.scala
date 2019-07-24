package forex.domain

case class Rate(
                 pair: Rate.Pair,
                 price: Price,
                 timestamp: Timestamp
               )

object Rate {

  final case class Pair(
                         from: Currency,
                         to: Currency
                       ) {
    def asSymbol = from.toString + to.toString
  }

  object Pair {
    def fromSymbol(s: String) = s.grouped(3).toSeq match {
      case Seq(from, to) => Pair(Currency.fromString(from), Currency.fromString(to))
    }
  }

}
