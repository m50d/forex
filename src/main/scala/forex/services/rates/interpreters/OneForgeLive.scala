package forex.services.rates.interpreters

import java.time.{Instant, OffsetDateTime, ZoneId}

import cats.effect.ConcurrentEffect
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.either._
import forex.config.OneForgeConfig
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.RatesServiceError
import io.circe.generic.auto._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.circe._
import forex.services.rates.RatesServiceAlgebra
import forex.services.rates.OneForgeLookupFailed

import scala.concurrent.ExecutionContext.Implicits.global

case class OneForgeQuote(symbol: String, price: BigDecimal, timestamp: Long)

class OneForgeLive[F[_] : ConcurrentEffect](config: OneForgeConfig) extends RatesServiceAlgebra[F] {
  val quotesUri: Uri = Uri.uri("https://forex.1forge.com/1.0.3/quotes")

  override def get(pair: Rate.Pair): F[RatesServiceError Either Rate] = {
    val uri = quotesUri.withQueryParam("pairs", pair.asSymbol).withQueryParam("api_key", config.apikey): Uri
    val request = Request[F](method = Method.GET, uri = uri)
    implicit val decoder = jsonOf[F, Seq[OneForgeQuote]]
    BlazeClientBuilder[F](global).resource.use { client =>
      client.fetch[RatesServiceError Either Rate](request) {
        case Status.Successful(r) => r.attemptAs[Seq[OneForgeQuote]].leftMap(mf => OneForgeLookupFailed(mf.message): RatesServiceError)
          .map { case Seq(ofq) => Rate(Rate.Pair.fromSymbol(ofq.symbol),
            Price(ofq.price), Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochSecond(ofq.timestamp), ZoneId.systemDefault()))
          )
          }
          .value
        case r => r.as[String].map(b => Left(OneForgeLookupFailed(s"Status ${r.status.code}, body $b")))
      }
    }
  }
}
