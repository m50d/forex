package forex.services.rates.populator

import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{Sync, Timer}
import cats.instances.vector._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import forex.domain.Rate
import scalacache._

import scala.concurrent.duration._

class SchedulingPopulator[F[_] : Sync : Mode : Timer](oneForgeService: OneForgeService[F])(implicit
  cache: Cache[Rate]) {
  // Some or all of these values could be moved to config, as and when we had a use case for changing them
  val desiredMaxAge = 5 minutes
  val assumedMaxFetchTime = 30 seconds
  val ageAtWhichToFetch = desiredMaxAge - assumedMaxFetchTime;
  // Exponential backoff or similar might be appropriate in a "real" system. Would likely be driven by what kind of
  // error behaviour we saw from 1forge
  val retryTimeAfterError = 30 seconds

  def stepOnce: F[FiniteDuration] = oneForgeService.getAll flatMap {
    case Left(error) ⇒
      // In a "real" system, would want some kind of alerting system (e.g. alert if x errors in y minutes)
      Sync[F].delay(System.err.println(error)).map(_ ⇒ retryTimeAfterError)
    case Right(results) ⇒ for {
      oldestResult <- results.traverse {
        rate: Rate ⇒ put(rate.pair.asSymbol)(rate).map(_ ⇒ rate.timestamp.value.toEpochSecond)
      }.map(_.min)
      now ← Timer[F].clock.realTime(TimeUnit.SECONDS)
      currentAge = FiniteDuration(now - oldestResult, TimeUnit.SECONDS)
    } yield ageAtWhichToFetch - currentAge
  }

  def go: F[Nothing] = Monad[F].tailRecM[Unit, Nothing](())(_ ⇒ stepOnce.flatMap(Timer[F].sleep).map(_ ⇒ Left(())))
}
