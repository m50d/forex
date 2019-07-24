package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.Monad
import cats.syntax.either._
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.flatMap._
import forex.domain.{Rate, RatePair}
import forex.services.rates.{RatesServiceAlgebra, RatesServiceError}
import scalacache._

class Caching[F[_] : Monad : Mode](inner: RatesServiceAlgebra[F])(implicit cache: Cache[Rate]) extends RatesServiceAlgebra[F] {
  override def get(pair: RatePair): F[RatesServiceError Either Rate] = scalacache.get(pair.asSymbol) flatMap {
    case Some(rate: Rate) if (rate.timestamp.value plusMinutes 5 isAfter OffsetDateTime.now()) =>
      rate.asRight[RatesServiceError].pure
    case _ => inner.get(pair) flatMap {
      case Left(error) => error.asLeft[Rate].pure
      case Right(rate) => put(pair.asSymbol)(rate).map { _ => rate.asRight }
    }
  }
}
