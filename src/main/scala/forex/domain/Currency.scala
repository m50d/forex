package forex.domain

import java.util.Locale

import cats.syntax.either._
import cats.Show

sealed trait Currency

object Currency {

  case object AUD extends Currency

  case object CAD extends Currency

  case object CHF extends Currency

  case object EUR extends Currency

  case object GBP extends Currency

  case object NZD extends Currency

  case object JPY extends Currency

  case object SGD extends Currency

  case object USD extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  def fromString(s: String): Either[String, Currency] = s.toUpperCase(Locale.ENGLISH) match {
    case "AUD" => AUD.asRight
    case "CAD" => CAD.asRight
    case "CHF" => CHF.asRight
    case "EUR" => EUR.asRight
    case "GBP" => GBP.asRight
    case "NZD" => NZD.asRight
    case "JPY" => JPY.asRight
    case "SGD" => SGD.asRight
    case "USD" => USD.asRight
    case x => Left(s"Unknown currency $x")
  }

  val all = Vector(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD)

}
