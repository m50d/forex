package forex.services.rates.interpreters

import java.time.{Instant, OffsetDateTime, ZoneId}

import cats.effect.ConcurrentEffect
import cats.syntax.functor._
import cats.syntax.either._
import forex.config.OneForgeConfig
import forex.domain.{Price, Rate, RatePair, Timestamp}
import forex.services.rates._
import io.circe.generic.auto._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.circe._

import scala.concurrent.ExecutionContext.Implicits.global

case class OneForgeQuote(symbol: String, price: BigDecimal, timestamp: Long) {
  def asRate: Either[String, Rate] =
    RatePair.fromSymbol(symbol) map { rp =>
      Rate(rp, Price(price), Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
        // Not clear what zone is appropriate here since the 1Forge API simply offers an Instant
        // But this is the API we were given
        ZoneId.systemDefault())))
    }
}

class OneForgeLive[F[_] : ConcurrentEffect](config: OneForgeConfig) extends RatesServiceAlgebra[F] {
  val quotesUri: Uri = Uri.uri("https://forex.1forge.com/1.0.3/quotes")

  override def get(pair: RatePair): F[RatesServiceError Either Rate] = {
    val uri = quotesUri.withQueryParam("pairs", pair.asSymbol).withQueryParam("api_key", config.apikey): Uri
    val request = Request[F](method = Method.GET, uri = uri)
    implicit val decoder = jsonOf[F, Seq[OneForgeQuote]]
    BlazeClientBuilder[F](global).resource.use { client =>
      client.fetch[RatesServiceError Either Rate](request) {
        case Status.Successful(r) =>
          r.attemptAs[Seq[OneForgeQuote]].leftMap[RatesServiceError](mf => OneForgeJsonParsingFailed(mf.message))
            .subflatMap {
              case Seq(ofq) => ofq.asRate.leftMap(FailedToConvertOneForgeResponseToDomainObject(_))
              case x => Left(FailedToConvertOneForgeResponseToDomainObject(s"Expected one response but got $x"))
            }.value
        case r => r.as[String].map(b => Left(OneForgeRequestError(s"Status ${r.status.code}, body $b")))
      }
    }
  }
}
