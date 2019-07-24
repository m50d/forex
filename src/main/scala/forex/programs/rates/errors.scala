package forex.programs.rates

import forex.services.rates.{OneForgeLookupFailed, RatesServiceError}

sealed trait RatesProgramError extends Exception

final case class RateLookupFailed(msg: String) extends RatesProgramError

object errors {

  def toProgramError(error: RatesServiceError): RatesProgramError = error match {
    case OneForgeLookupFailed(msg) => RateLookupFailed(msg)
  }
}
