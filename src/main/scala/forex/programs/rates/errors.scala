package forex.programs.rates

sealed trait RatesProgramError extends Exception

final case class RateLookupFailed(msg: String) extends Exception(msg) with RatesProgramError
