package forex.services.rates

import forex.programs.rates.{RateLookupFailed, RatesProgramError}

sealed abstract class RatesServiceError(msg: String) {
  def toProgramError: RatesProgramError = RateLookupFailed(msg)
}
final case class OneForgeLookupFailed(msg: String) extends RatesServiceError(msg)
