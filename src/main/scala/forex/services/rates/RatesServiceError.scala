package forex.services.rates

import forex.programs.rates.{RatesProgramError, SystemOrProgrammingError}

sealed trait RatesServiceError {
  def toProgramError: RatesProgramError = SystemOrProgrammingError(toString)
}

final case class RateNotAvailable() extends RatesServiceError

