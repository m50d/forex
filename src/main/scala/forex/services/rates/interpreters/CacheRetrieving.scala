package forex.services.rates.interpreters

import cats.Monad
import cats.effect.Clock
import cats.syntax.functor._
import forex.domain.{Rate, RatePair}
import forex.services.rates.{RateNotAvailable, RatesServiceAlgebra, RatesServiceError}
import scalacache._

class CacheRetrieving[F[_] : Monad : Mode : Clock](implicit cache: Cache[Rate]) extends RatesServiceAlgebra[F] {
  override def get(pair: RatePair): F[RatesServiceError Either Rate] =
    scalacache.get(pair.asSymbol).map(_.toRight(RateNotAvailable()))
}
