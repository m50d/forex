package forex.services.rates.oneforge

sealed trait OneForgeServiceError

final case class OneForgeRequestError(msg: String) extends OneForgeServiceError
final case class OneForgeJsonParsingFailed(msg: String) extends OneForgeServiceError
final case class FailedToConvertOneForgeResponseToDomainObject(msg: String) extends OneForgeServiceError
