package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(http: HttpConfig, oneforge: OneForgeConfig)

case class HttpConfig(host: String, port: Int, timeout: FiniteDuration)

case class OneForgeConfig(apikey: String)