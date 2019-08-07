package forex.services.rates.populator

import java.util.concurrent.TimeUnit

import cats.effect.{Sync, Timer}
import cats.instances.vector._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import forex.domain.Rate
import scalacache._

import scala.concurrent.duration._

class SchedulingPopulator[F[_] : Sync : Mode : Timer](oneForgeService: OneForgeService[F])(implicit
  cache: Cache[Rate]) {
  // might consider exponential backoff etc.
  val retryTimeAfterError = 30 seconds
  val desiredMaxAge = 5 minutes
  val assumedMaxFetchTime = 30 seconds
  val ageAtWhichToFetch = desiredMaxAge - assumedMaxFetchTime;

  def stepOnce: F[FiniteDuration] = oneForgeService.getAll flatMap {
    case Left(error) ⇒
      // Would consider logging system, alerting etc.
      Sync[F].delay(System.err.println(error)).map(_ ⇒ retryTimeAfterError)
    case Right(results) ⇒ for {
      oldestResult <- results.traverse {
        rate: Rate ⇒ put(rate.pair.asSymbol)(rate).map(_ ⇒ rate.timestamp.value.toEpochSecond)
      }.map(_.min)
      now ← Timer[F].clock.realTime(TimeUnit.SECONDS)
      currentAge = FiniteDuration(now - oldestResult, TimeUnit.SECONDS)
    } yield ageAtWhichToFetch - currentAge
  }

  def go: F[Unit] = stepOnce.flatMap(Timer[F].sleep) *> go
}
