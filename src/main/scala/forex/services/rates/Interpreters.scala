package forex.services.rates

import cats.Applicative
import cats.effect.ConcurrentEffect
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative](): Algebra[F] = new OneForgeDummy[F]()
  def live[F[_]: ConcurrentEffect](): Algebra[F] = new OneForgeLive[F]()
}
