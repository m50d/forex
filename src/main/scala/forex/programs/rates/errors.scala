package forex.programs.rates

sealed trait RatesProgramError
final case class InvalidRequest(msg: String) extends RatesProgramError
final case class SystemOrProgrammingError(msg: String) extends RatesProgramError
