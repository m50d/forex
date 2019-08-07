package forex.services.rates.populator

import cats.effect.{ExitCode, IO, IOApp}
import forex.config.ApplicationConfig
import pureconfig.generic.auto._

object FetchQuotesIT extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val config = pureconfig.loadConfigOrThrow[ApplicationConfig]("app")
    val service = new OneForgeService[IO](config.oneforge)
    for {
      results ← service.getAll
      _ ← IO.delay(println(results))
    } yield ExitCode.Success
  }

}
