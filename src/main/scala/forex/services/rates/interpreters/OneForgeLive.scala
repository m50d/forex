package forex.services.rates.interpreters

import cats.effect.ConcurrentEffect
import cats.syntax.applicative._
import cats.syntax.either._
import forex.config.OneForgeConfig
import forex.domain.Rate
import forex.services.rates.errors.Error
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Uri}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneForgeLookupFailed

import scala.concurrent.ExecutionContext.Implicits.global

class OneForgeLive[F[_] : ConcurrentEffect](config: OneForgeConfig) extends Algebra[F] {
  val quotesUri: Uri = Uri.uri("https://forex.1forge.com/1.0.3/quotes")

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val uri = quotesUri.withQueryParam("pairs", pair.asSymbol).withQueryParam("api_key", config.apikey): Uri
    val request = Request[F](method = Method.GET, uri = uri)
    BlazeClientBuilder[F](global).resource.use { client =>
      client.fetch[Error Either Rate](request)(response => (OneForgeLookupFailed("unimplemented"): Error).asLeft[Rate].pure)
    }
  }
}
