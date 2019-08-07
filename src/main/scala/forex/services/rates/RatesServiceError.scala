package forex.services.rates

import forex.programs.rates.{RatesProgramError, SystemOrProgrammingError}

sealed trait RatesServiceError {
  def toProgramError: RatesProgramError = SystemOrProgrammingError(toString)
}

final case class OneForgeRequestError(msg: String) extends RatesServiceError
final case class OneForgeJsonParsingFailed(msg: String) extends RatesServiceError
final case class FailedToConvertOneForgeResponseToDomainObject(msg: String) extends RatesServiceError
final case class RateNotAvailable() extends RatesServiceError

