package forex

package object services {
  type RatesService[F[_]] = rates.RatesServiceAlgebra[F]
}
