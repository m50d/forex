package forex.services.rates.interpreters

import java.time.ZonedDateTime

import cats.Id
import cats.effect.Clock
import forex.domain.{Price, Rate, RatePair, Timestamp}
import forex.services.Caches
import forex.services.rates.{OneForgeRequestError, RatesServiceAlgebra}
import org.easymock.EasyMock._
import org.junit.Assert.assertEquals
import org.junit.Test
import scalacache.modes.sync._

import scala.concurrent.duration.TimeUnit

class CachingTest {
  @Test def basicFunctionality(): Unit = {
    val nowForTest = ZonedDateTime.parse("2019-05-06T12:34:17Z")
    implicit object StubClock extends Clock[Id] {
      override def realTime(unit: TimeUnit): Id[Long] = nowForTest.toInstant.getEpochSecond
      override def monotonic(unit: TimeUnit): Id[Long] = ???
    }
    implicit val cache = Caches.guavaRates
    val mockAlgebra = mock[RatesServiceAlgebra[Id]](classOf[RatesServiceAlgebra[Id]])
    val caching = new Caching[Id](mockAlgebra)

    val eurusd = RatePair.fromSymbol("EURUSD").right.get
    val usdjpy = RatePair.fromSymbol("USDJPY").right.get
    val eurjpy = RatePair.fromSymbol("EURJPY").right.get

    val error = Left(OneForgeRequestError("Test error"))
    val success = Right(Rate(RatePair.fromSymbol("EURUSD").right.get, Price(4L),
      Timestamp(nowForTest.minusMinutes(1).toOffsetDateTime)
    ))
    val expiredSuccess = Right(Rate(RatePair.fromSymbol("USDJPY").right.get, Price(4L),
      Timestamp(nowForTest.minusMinutes(6).toOffsetDateTime))
    )
    expect(mockAlgebra.get(eurusd)).andReturn(error).andReturn(success)
    expect(mockAlgebra.get(usdjpy)).andReturn(success)
    expect(mockAlgebra.get(eurjpy)).andReturn(expiredSuccess).andReturn(success)
    replay(mockAlgebra)
    assertEquals(error, caching.get(eurusd))
    assertEquals(success, caching.get(eurusd))
    assertEquals(success, caching.get(usdjpy))
    assertEquals(success, caching.get(usdjpy))
    assertEquals(expiredSuccess, caching.get(eurjpy))
    assertEquals(success, caching.get(eurjpy))
    assertEquals(success, caching.get(eurjpy))
    verify(mockAlgebra)
  }
}
