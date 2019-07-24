package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.Id
import forex.domain.{Price, Rate, RatePair, Timestamp}
import forex.services.Caches
import forex.services.rates.{OneForgeRequestError, RatesServiceAlgebra}
import org.junit.Test
import org.easymock.EasyMock._
import scalacache.modes.sync._
import org.junit.Assert.assertEquals

class CachingTest {
  @Test def basicFunctionality(): Unit = {
    implicit val cache = Caches.guavaRates
    val mockAlgebra = mock[RatesServiceAlgebra[Id]](classOf[RatesServiceAlgebra[Id]])
    val caching = new Caching[Id](mockAlgebra)

    val eurusd = RatePair.fromSymbol("EURUSD").right.get
    val usdjpy = RatePair.fromSymbol("EURJPY").right.get

    val error = Left(OneForgeRequestError("Test error"))
    val success = Right(Rate(RatePair.fromSymbol("EURUSD").right.get, Price(4L),
      Timestamp(OffsetDateTime.MAX.minusDays(1)))
    )
    expect(mockAlgebra.get(eurusd)).andReturn(error).andReturn(success)
    expect(mockAlgebra.get(usdjpy)).andReturn(success)
    replay(mockAlgebra)
    assertEquals(error, caching.get(eurusd))
    assertEquals(success, caching.get(eurusd))
    assertEquals(success, caching.get(usdjpy))
    assertEquals(success, caching.get(usdjpy))
    verify(mockAlgebra)
  }
}
