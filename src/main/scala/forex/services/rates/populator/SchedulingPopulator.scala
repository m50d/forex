package forex.services.rates.populator

import cats.Monad
import cats.effect.Timer
import cats.syntax.flatMap._
import cats.syntax.functor._

class SchedulingPopulator[F[_] : Monad : Timer](populator: CachePopulator[F]) {
  def go: F[Nothing] = Monad[F].tailRecM[Unit, Nothing](())(_ ⇒ populator.populate.flatMap(Timer[F].sleep).map(_ ⇒
    Left(())))
}
