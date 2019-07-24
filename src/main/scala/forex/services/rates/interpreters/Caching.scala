package forex.services.rates.interpreters

import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.Clock
import cats.syntax.either._
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.flatMap._
import forex.domain.{Rate, RatePair, Timestamp}
import forex.services.rates.{RatesServiceAlgebra, RatesServiceError}
import scalacache._

class Caching[F[_] : Monad : Mode : Clock](inner: RatesServiceAlgebra[F])(implicit cache: Cache[Rate]) extends RatesServiceAlgebra[F] {
  override def get(pair: RatePair): F[RatesServiceError Either Rate] =
    (scalacache.get(pair.asSymbol), Clock[F].realTime(TimeUnit.SECONDS)).tupled flatMap {
      case (Some(rate: Rate), now) if rate.timestamp.value plusMinutes 5 isAfter Timestamp(now).value =>
        rate.asRight[RatesServiceError].pure
      case _ => inner.get(pair) flatMap {
        case Left(error) => error.asLeft[Rate].pure
        case Right(rate) => put(pair.asSymbol)(rate).map { _ => rate.asRight }
      }
    }
}
