package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_] : ConcurrentEffect : Timer](config: ApplicationConfig) {
  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware = HttpApp[F] => HttpApp[F]

  private[this] val ratesService: RatesService[F] = RatesServices.live[F](config.oneforge)
  private[this] val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)
  private[this] val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes
  private[this] val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] => AutoSlash(http) }
  }
  private[this] val appMiddleware: TotalMiddleware = { http: HttpApp[F] => Timeout(config.http.timeout)(http) }
  private[this] val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)
}
