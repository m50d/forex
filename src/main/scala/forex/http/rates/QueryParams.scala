package forex.http.rates

import cats.data.ValidatedNel
import cats.syntax.either._
import forex.domain.Currency
import org.http4s.{ParseFailure, QueryParamDecoder, QueryParameterValue}
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit object CurrencyQueryParam extends QueryParamDecoder[Currency] {
    override def decode(value: QueryParameterValue): ValidatedNel[ParseFailure, Currency] =
      Currency.fromString(value.value).leftMap(s => ParseFailure("Unknown currency", s)).toValidatedNel
  }

  object FromQueryParam extends QueryParamDecoderMatcher[Currency]("from")

  object ToQueryParam extends QueryParamDecoderMatcher[Currency]("to")

}
