package forex.services.rates

sealed trait RatesServiceError

final case class OneForgeLookupFailed(msg: String) extends RatesServiceError
