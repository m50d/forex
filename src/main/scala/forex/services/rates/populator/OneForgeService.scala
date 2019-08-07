package forex.services.rates.populator

import cats.effect.ConcurrentEffect
import cats.instances.either._
import cats.instances.vector._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import forex.config.OneForgeConfig
import forex.domain.{Price, Rate, RatePair, Timestamp}
import forex.services.rates._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}

import scala.concurrent.ExecutionContext.Implicits.global

case class OneForgeQuote(symbol: String, price: BigDecimal, timestamp: Long) {
  def asRate: Either[String, Rate] =
    RatePair.fromSymbol(symbol) map { rp => Rate(rp, Price(price), Timestamp(timestamp)) }
}

class OneForgeService[F[_] : ConcurrentEffect](config: OneForgeConfig) {
  val quotesUri: Uri = Uri.uri("https://forex.1forge.com/1.0.3/quotes")

  def get(pairs: Vector[RatePair]): F[RatesServiceError Either Vector[Rate]] = {
    val pairsParam = pairs.map(_.asSymbol).mkString(",")
    val uri = quotesUri.withQueryParam("pairs", pairsParam).withQueryParam("api_key", config.apikey): Uri
    val request = Request[F](method = Method.GET, uri = uri)
    implicit val decoder = jsonOf[F, Vector[OneForgeQuote]]
    BlazeClientBuilder[F](global).resource.use { client =>
      client.fetch[RatesServiceError Either Vector[Rate]](request) {
        case Status.Successful(r) =>
          r.attemptAs[Vector[OneForgeQuote]].leftMap[RatesServiceError](mf => OneForgeJsonParsingFailed(mf.message))
            .subflatMap(_.traverse(_.asRate.leftMap(FailedToConvertOneForgeResponseToDomainObject(_)))).value
        case r => r.as[String].map(b => Left(OneForgeRequestError(s"Status ${r.status.code}, body $b")))
      }
    }
  }

  def getAll = get(RatePair.all)
}