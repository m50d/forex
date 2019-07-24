package forex.services.rates

import forex.programs.rates.{RateLookupFailed, RatesProgramError}

sealed trait RatesServiceError {
  def toProgramError: RatesProgramError = RateLookupFailed(toString)
}

final case class OneForgeRequestError(msg: String) extends RatesServiceError
final case class OneForgeJsonParsingFailed(msg: String) extends RatesServiceError
final case class FailedToConvertOneForgeResponseToDomainObject(msg: String) extends RatesServiceError

