package forex.services.rates.interpreters

import forex.services.rates.{RatesServiceAlgebra, RatesServiceError}
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, RatePair, Timestamp}

class OneForgeDummy[F[_] : Applicative] extends RatesServiceAlgebra[F] {

  override def get(pair: RatePair): F[RatesServiceError Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[RatesServiceError].pure[F]

}
