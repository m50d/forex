package forex.services.rates.oneforge

import cats.effect.{ConcurrentEffect, Sync}
import cats.instances.either._
import cats.instances.vector._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.traverse._
import forex.config.OneForgeConfig
import forex.domain.{Price, Rate, RatePair, Timestamp}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

case class OneForgeQuote(symbol: String, price: BigDecimal, timestamp: Long) {
  def asRate: Either[String, Rate] =
    RatePair.fromSymbol(symbol) map { rp => Rate(rp, Price(price), Timestamp(timestamp)) }
}

class OneForgeService[F[_] : ConcurrentEffect](config: OneForgeConfig) {
  // Could be moved to config, as and when we had a use case for changing this
  private[this] val quotesUri: Uri = Uri.uri("https://forex.1forge.com/1.0.3/quotes")

  private[this] val log = LoggerFactory.getLogger(getClass)

  def get(pairs: Vector[RatePair]): F[OneForgeServiceError Either Vector[Rate]] = {
    val pairsParam = pairs.map(_.asSymbol).mkString(",")
    val uri = quotesUri.withQueryParam("pairs", pairsParam).withQueryParam("api_key", config.apikey): Uri
    val request = Request[F](method = Method.GET, uri = uri)
    implicit val decoder = jsonOf[F, Vector[OneForgeQuote]]
    Sync[F].delay(log.info(s"Running fetch for $pairs")) *>
      BlazeClientBuilder[F](global).resource.use { client =>
        client.fetch[OneForgeServiceError Either Vector[Rate]](request) {
          case Status.Successful(r) =>
            r.attemptAs[Vector[OneForgeQuote]].leftMap[OneForgeServiceError](mf => OneForgeJsonParsingFailed(mf.message))
              .subflatMap(_.traverse(_.asRate.leftMap(FailedToConvertOneForgeResponseToDomainObject(_)))).value
          case r => r.as[String].map(b => Left(OneForgeRequestError(s"Status ${r.status.code}, body $b")))
        }
      }
  }

  def getAll = get(RatePair.all)
}