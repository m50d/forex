package forex.domain

import java.time.{Instant, OffsetDateTime, ZoneId}

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)
  def apply(epochSeconds: Long) : Timestamp =
    Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds),
      // Not clear what zone is appropriate here since we are really talking about a physical instant
      // But this is the API we were given
      ZoneId.systemDefault()))
}
