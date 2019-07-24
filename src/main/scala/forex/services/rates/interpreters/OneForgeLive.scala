package forex.services.rates.interpreters

import cats.effect.ConcurrentEffect
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.Rate
import forex.services.rates.errors.Error
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneForgeLookupFailed

import scala.concurrent.ExecutionContext.Implicits.global

class OneForgeLive[F[_] : ConcurrentEffect] extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val request = Request[F](method = Method.GET)
    BlazeClientBuilder[F](global).resource.use { client =>
      client.fetch[Error Either Rate](request)(response => (OneForgeLookupFailed("unimplemented"): Error).asLeft[Rate].pure)
    }
  }
}
