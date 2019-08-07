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
  (e.g. I reversed the order of `from`/`to` based on a manual sanity check -
  I don't think there would be any value in a unit test for that)
  * Usually I would try to use non-mock-based call/result unit tests
  to check any pure business logic that was complex enough to have potential errors.
  In this case the cache populator was the only component that had any logic
  that I felt the need to test.
  Since I had an existing mock-based test from my previous efforts,
  I found it easier to adapt that test to the new design.
  Thus the current test is mock-based.
  * The cache populator is somewhat tightly coupled to the OneForge service -
  there is no interface between them and the populator does have access to
  OneForge-specific error types (even if it only logs them at present).
  I would introduce an interface as and when we had more than one implementation
  (which goes hand in hand with the previous point -
  for a non-mock test I would likely want a stub implementation)
  but when we only have one implementation I think the (code) overhead
  of an extra layer would outweigh the advantages.
  * The current implementation doesn't cache any failed calls
  and schedules them for a retry after 30 seconds.
  This way even if we are continuously retrying,
  we will only perform 2880 requests/day to 1forge,
  staying within the 5000 requests/day limit.
