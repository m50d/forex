package forex.services

import com.google.common.cache.CacheBuilder
import forex.domain.Rate
import scalacache._
import scalacache.guava._


object Caches {
  def guava: Cache[Rate] =
    GuavaCache(CacheBuilder.newBuilder().maximumSize(10000L).build[String, Entry[Rate]])
}
