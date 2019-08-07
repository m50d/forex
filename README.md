# Forex

## Launching

  * Set your API key either in `application.conf`
   or via a JVM property (`-Dapp.oneforge.apikey=...`) 
  * Run via running Main in IDE, or `mvn compile exec:java`,
  or build an executable jar (`mvn package`)
  and run with  `java -jar target/forex-0.1-SNAPSHOT.jar`
  * Listens on the configured IP/port
  
## Implementation notes

  * Generally I kept things as simple/minimal-effort as possible.
  I used existing libraries for the HTTP client and caching.
  Max cache size is hardcoded for the time being;
  I would only add a configuration parameter if and when there was a
  clear need for it. 
  * I tried to keep the code close to the existing style
  in terms of using final tagless etc.
  However I did  rename the repeated types with the same name
  (`Error` and `Algebra` in different packages)
  as I found they were confusing me to the point of making code errors.
  I also moved some classes out of `object`s
  (e.g. `RatePair` rather than `Rate.Pair`).
  * I moved the build to Maven
  as I didn't have reliable IDE integration with SBT,
  and wanted a view of the dependency tree when adding new dependencies
  (particularly as I'd had issues on the "user" project with
  e.g. inconsistent versions of cats).
  Of course on a real project I'd align with the rest of the team,
  and there would likely be less call for adding more dependencies,
  but for this test I wanted dependency management out of the way
  so that I could focus on the coding.
  * I assumed that the "free tier" of 1forge meant the free trial of the
  starter tier, as that was the only free option I saw.
  I made use of the first API on the page since it seemed like it might
  work, and the results seem good enough so I see no value in changing
  it (even though I've now seen a "simpler" API further down the page).
  * With error handling I tried to draw a distinction
  between known/expected errors (`InvalidRequest`)
  and unexpected exceptions (`SystemOrProgrammingError`).
  I find that it's worth drawing a distinction between "4xx" and "5xx"
  in HTTP terms, but possibly not worth going into much further detail
  as finer-grained distinctions are not generally useful to the client.
  I didn't actually think of any "4xx" cases so there are no code paths
  that actually use `InvalidRequest`.
  The case of an invalid currency code is handled by an explicit route
  because that was a case I encountered myself when manually testing.
  Cases where a currency code is known-to-us but not supported by 1forge
  might be a good case for `InvalidRequest`;
  I assumed the given `Currency` values are known to be supported
  and did *not* test every case myself.
  * "4xx" errors should send details to the client in most cases, IMO.
  A "5xx" error represents an unanticipated situation so that decision
  is more dependent on what the audience is -
  e.g. a public-facing service likely should not provide the details
  of unknown exceptions to clients.
  In this case I included error details for ease of debugging,
  but would flag that up to reassess if "productionising".
  * For code that does not contain any business logic and is well typed,
  I relied solely on a manual (Postman) "smoke test" as I suspect the only
  possible errors would be misunderstandings of interfaces etc.
  (e.g. I just reversed the order of `from`/`to` based on a manual
  sanity check -
  I don't think there would be any value in a unit test for that)
  * I wrote a mock-based unit test for the caching service
  as that was the area where I had the least confidence.
  While I'm not normally a fan of mock-based tests,
  I found it was the easiest way to test that the cache was actually
  caching results, since the nature of a cache is to produce similarly
  correct behaviour as an implementation without the cache would.
  I also did a manual test of caching behaviour
  against the number of requests shown as used on the 1forge account.
  Usually I would try to use non-mock-based call/result unit tests
  to check any pure business logic that was complex enough to have
  potential errors; however I don't think any such complex logic was
  warranted here.
  * The current implementation doesn't cache any failed calls.
  This can be a difficult judgement call as it would be easy to hit
  the call limits on 1forge due to repeated requests if we e.g. had
  an error in our parsing logic. However if there is a genuine error
  with the 1forge responses then we certainly don't want to cache those.
  * This implementation caches quotes for 5 minutes
  (the maximum permitted by the requirements), meaning that it will make
  at most 720 requests/day for a given currency pair.
  So as long as at most ~7 currency pairs are requested frequently,
  this service will be able to stay within the 5000 requests/day limit
  of the 1forge free tier. A more sophisticated implementation could
  batch up several currency pairs into each 1forge request, or make use
  of cached quotes for one currency pair to quote the reverse pair.
  However I don't think any such more complex logic is justified by
  the requirements currently given.
