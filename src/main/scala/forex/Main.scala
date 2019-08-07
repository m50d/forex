package forex

import cats.effect._
import cats.syntax.functor._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import scalacache.Mode

object Main extends IOApp {

  import scalacache.CatsEffect.modes.async

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)

}

class Application[F[_] : ConcurrentEffect : Timer : Mode] {

  def stream: Stream[F, Unit] =
    for {
      config ← Config.stream("app")
      module = new Module[F](config)
      _ ← Stream.emit(Concurrent[F].start(module.populator.go))
      _ ← BlazeServerBuilder[F]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(module.httpApp)
        .serve
    } yield ()

}
