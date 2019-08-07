package forex.services.rates.populator

import java.util.concurrent.TimeUnit

import cats.effect.{Clock, Sync, Timer}
import cats.instances.vector._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import forex.domain.Rate
import forex.services.rates.oneforge.OneForgeService
import org.slf4j.LoggerFactory
import scalacache.{Cache, Mode, put}

import scala.concurrent.duration._

class CachePopulator[F[_] : Sync : Mode : Clock](oneForgeService: OneForgeService[F])(implicit cache: Cache[Rate]) {
  // Some or all of these values could be moved to config, as and when we had a use case for changing them
  private[this] val desiredMaxAge = 5 minutes
  private[this] val assumedMaxFetchTime = 30 seconds
  private[this] val ageAtWhichToFetch = desiredMaxAge - assumedMaxFetchTime;
  // 30 seconds means we will stay within the 5000 requests/day limit even if every request is erroring
  // Exponential backoff or similar might be appropriate in a "real" system. Would likely be driven by what kind of
  // error behaviour we saw from 1forge
  private[this] val retryTimeAfterError = 30 seconds

  private[this] val log = LoggerFactory.getLogger(getClass)

  /**
   * @return duration after which we should repopulate the cache
   */
  def populate: F[FiniteDuration] = oneForgeService.getAll flatMap {
    case Left(error) ⇒
      // In a "real" system, would want some kind of alerting system (e.g. alert if x errors in y minutes)
      Sync[F].delay(log.error(error.toString)).map(_ ⇒ retryTimeAfterError)
    case Right(results) ⇒ for {
      oldestResult <- results.traverse {
        rate: Rate ⇒ put(rate.pair.asSymbol)(rate).map(_ ⇒ rate.timestamp.value.toEpochSecond)
      }.map(_.min)
      now ← Clock[F].realTime(TimeUnit.SECONDS)
      currentAge = FiniteDuration(now - oldestResult, TimeUnit.SECONDS)
    } yield ageAtWhichToFetch - currentAge
  }
}
