package forex.services.rates.populator

import java.time.ZonedDateTime

import cats.Id
import cats.effect.{Clock, ExitCase, Sync, Timer}
import forex.domain.{Price, Rate, RatePair, Timestamp}
import forex.services.Caches
import forex.services.rates.RateNotAvailable
import forex.services.rates.interpreters.CacheRetrieving
import forex.services.rates.oneforge.{OneForgeRequestError, OneForgeService}
import org.easymock.EasyMock._
import org.junit.Assert.assertEquals
import org.junit.Test
import scalacache.modes.sync._

import scala.concurrent.duration._

class CachePopulatorTest {
  @Test def basicFunctionality(): Unit = {
    val nowForTest = ZonedDateTime.parse("2019-05-06T12:34:17Z")
    implicit object StubClock extends Clock[Id] {
      override def realTime(unit: TimeUnit): Id[Long] = nowForTest.toInstant.getEpochSecond

      override def monotonic(unit: TimeUnit): Id[Long] = ???
    }
    implicit val cache = Caches.guavaRates
    val mockService = mock[OneForgeService[Id]](classOf[OneForgeService[Id]])
    implicit object Yolo extends Sync[Id] {
      override def suspend[A](thunk: ⇒ Id[A]) = thunk

      override def bracketCase[A, B](acquire: Id[A])(use: A ⇒ Id[B])(release: (A, ExitCase[Throwable]) ⇒ Id[Unit]) = ???

      override def raiseError[A](e: Throwable) = ???

      override def handleErrorWith[A](fa: Id[A])(f: Throwable ⇒ Id[A]) = ???

      override def flatMap[A, B](fa: Id[A])(f: A ⇒ Id[B]) = f(fa)

      override def tailRecM[A, B](a: A)(f: A ⇒ Id[Either[A, B]]) = ???

      override def pure[A](x: A) = x
    }

    val populator = new CachePopulator[Id](mockService)
    val retriever = new CacheRetrieving[Id]()

    val eurusd = RatePair.fromSymbol("EURUSD").right.get
    val usdjpy = RatePair.fromSymbol("USDJPY").right.get
    val eurjpy = RatePair.fromSymbol("EURJPY").right.get

    val populationError = Left(OneForgeRequestError("Test error"))
    val retrievalError = Left(RateNotAvailable())
    val recentResult = Rate(RatePair.fromSymbol("EURUSD").right.get, Price(4L), Timestamp(nowForTest.minusMinutes(1)
      .toOffsetDateTime)
    )
    val oldResult = Rate(RatePair.fromSymbol("USDJPY").right.get, Price(4L),
      Timestamp(nowForTest.minusMinutes(3).toOffsetDateTime))
    val success = Right(Vector(recentResult))
    val expiredSuccess = Right(Vector(recentResult, oldResult))
    expect(mockService.getAll).andReturn(populationError).andReturn(success).andReturn(expiredSuccess)
    replay(mockService)
    assertEquals(retrievalError, retriever.get(eurusd))
    // next retrieval is 30 seconds after error
    assertEquals(30 seconds, populator.populate)
    assertEquals(retrievalError, retriever.get(eurusd))
    // result is 1 minute old, next retrieval is 3 minutes 30 seconds
    assertEquals(3.minutes + 30.seconds, populator.populate)
    assertEquals(Right(recentResult), retriever.get(eurusd))
    assertEquals(retrievalError, retriever.get(usdjpy))
    // result is 3 minutes old, next retrieval is 1 minute 30 seconds
    assertEquals(1.minute + 30.seconds, populator.populate)
    assertEquals(Right(oldResult), retriever.get(usdjpy))
    assertEquals(retrievalError, retriever.get(eurjpy))
    verify(mockService)
  }
}
