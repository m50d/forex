package forex.services.rates

import cats.Applicative
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative](): RatesServiceAlgebra[F] = new OneForgeDummy[F]()
//  def liveDirect[F[_]: ConcurrentEffect](config: OneForgeConfig): RatesServiceAlgebra[F] = new OneForgeService[F](config)
}
