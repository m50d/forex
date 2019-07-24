package forex

package object services {
  type RatesService[F[_]] = rates.RatesServiceAlgebra[F]
  final val RatesServices = rates.Interpreters
}
