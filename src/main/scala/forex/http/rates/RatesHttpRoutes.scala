package forex.http
package rates

import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{InvalidRequest, SystemOrProgrammingError, Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.slf4j.LoggerFactory

class RatesHttpRoutes[F[_] : Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {
  import Converters._, QueryParams._, Protocol._

  private[this] val log = LoggerFactory.getLogger(getClass)
  private[http] val prefixPath = "/rates"

  private[this] val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
        case Right(rate) => Ok(rate.asGetApiResponse)
        case Left(InvalidRequest(msg)) => BadRequest(msg)
        case Left(SystemOrProgrammingError(msg)) =>
          Sync[F].delay(log.error(msg)) *> InternalServerError(msg)
      }
    case GET -> Root =>
      BadRequest("""Missing or invalid "from" or "to" currencies""")
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
