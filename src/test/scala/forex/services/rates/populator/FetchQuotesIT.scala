package forex.services.rates.populator

import cats.effect.{ExitCode, IO, IOApp}
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.services.Caches
import forex.services.rates.oneforge.OneForgeService
import pureconfig.generic.auto._
import scalacache.Cache
import scalacache.CatsEffect.modes.async

object FetchQuotesIT extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val cache: Cache[Rate] = Caches.guavaRates
    val config = pureconfig.loadConfigOrThrow[ApplicationConfig]("app")
    val service = new OneForgeService[IO](config.oneforge)
    val populator = new CachePopulator[IO](service)
    for {
      results ← service.getAll
      _ ← IO.delay(println(results))
      timeToDelay ← populator.populate
      _ ← IO.delay(println(timeToDelay))
    } yield ExitCode.Success
  }

}
