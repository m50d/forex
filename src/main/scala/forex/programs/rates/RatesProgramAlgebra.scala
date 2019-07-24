package forex.programs.rates

import forex.domain.Rate

trait RatesProgramAlgebra[F[_]] {
  def get(request: Protocol.GetRatesRequest): F[RatesProgramError Either Rate]
}
