package forex.services.rates.interpreters

import forex.domain.{Rate, RatePair}
import forex.services.rates.{RatesServiceAlgebra, RatesServiceError}

class Cacheing[F[_]](inner: RatesServiceAlgebra[F]) extends RatesServiceAlgebra[F] {
  override def get(pair: RatePair): F[Either[RatesServiceError, Rate]] = ???
}
