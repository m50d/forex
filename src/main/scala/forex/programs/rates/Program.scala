package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services.RatesService

class Program[F[_]: Functor](
    ratesService: RatesService[F]
) extends RatesProgramAlgebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[RatesProgramError Either Rate] =
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError(_)).value

}

object Program {

  def apply[F[_]: Functor](
      ratesService: RatesService[F]
  ): RatesProgramAlgebra[F] = new Program[F](ratesService)

}
