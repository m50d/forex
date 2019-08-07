package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import forex.services.rates.oneforge.OneForgeService
import forex.services.rates.interpreters.CacheRetrieving
import forex.services.rates.populator.{CachePopulator, SchedulingPopulator}
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import scalacache.{Cache, Mode}

class Module[F[_] : ConcurrentEffect : Timer: Mode](config: ApplicationConfig) {
  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware = HttpApp[F] => HttpApp[F]

  private[this] implicit val cache: Cache[Rate] = Caches.guavaRates

  private[this] val oneForgeService: OneForgeService[F] = new OneForgeService[F](config.oneforge)
  private[this] val cachePopulator: CachePopulator[F] = new CachePopulator[F](oneForgeService)
  private[this] val ratesService: RatesService[F] = new CacheRetrieving[F]
  private[this] val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)
  private[this] val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes
  private[this] val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] => AutoSlash(http) }
  }
  private[this] val appMiddleware: TotalMiddleware = { http: HttpApp[F] => Timeout(config.http.timeout)(http) }
  private[this] val http: HttpRoutes[F] = ratesHttpRoutes

  val populator: SchedulingPopulator[F] = new SchedulingPopulator[F](cachePopulator)
  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)
}
