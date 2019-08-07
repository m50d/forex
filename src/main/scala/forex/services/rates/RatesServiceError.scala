package forex.services.rates

import forex.programs.rates.{RatesProgramError, SystemOrProgrammingError}

sealed trait RatesServiceError {
  def toProgramError: RatesProgramError = SystemOrProgrammingError(toString)
}

final case class RateNotAvailable() extends RatesServiceError

sealed trait OneForgeServiceError

final case class OneForgeRequestError(msg: String) extends OneForgeServiceError
final case class OneForgeJsonParsingFailed(msg: String) extends OneForgeServiceError
final case class FailedToConvertOneForgeResponseToDomainObject(msg: String) extends OneForgeServiceError

