package forex.services.rates

import forex.domain.Rate

trait RatesServiceAlgebra[F[_]] {
  def get(pair: Rate.Pair): F[RatesServiceError Either Rate]
}
