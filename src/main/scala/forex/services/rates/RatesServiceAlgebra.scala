package forex.services.rates

import forex.domain.{Rate, RatePair}

trait RatesServiceAlgebra[F[_]] {
  def get(pair: RatePair): F[RatesServiceError Either Rate]
}
