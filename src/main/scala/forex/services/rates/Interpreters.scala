package forex.services.rates

import cats.Applicative
import cats.effect.ConcurrentEffect
import forex.config.OneForgeConfig
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative](): RatesServiceAlgebra[F] = new OneForgeDummy[F]()
  def live[F[_]: ConcurrentEffect](config: OneForgeConfig): RatesServiceAlgebra[F] = new OneForgeLive[F](config)
}
