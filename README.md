# Running

To run the application use `./sbt run`.

To run tests use `./sbt test`.

The configuration file at `src/main/resources/application.conf` contains several settings
that can be adjusted.

# Overview

In the abscence of specific requirements regarding frameworks used, I decided to go with Akka streams.
Since asynchronous producers are involved, streaming seems to fit the task naturally.

The idea is to adapt `CharacterReader` interface to become an Akka `Source` and build a stream
to perform the given task.

I only added short comments to indicate classes purpose, otherwise I let signatures speak for themselves.

To finish in a reasonable amount of time, some functionality has been intentionally omitted:

- there is no logging and no error reporting
- support for special characters is limited
- ActorSystem is slow to shutdown after hitting ctrl-c
- the reported results are eventual in nature due to Akka operators' internal buffers, but it is close enough

I followed TDD process closely. The only exception is `App` which I consider a boundary to the solution.
It would take significant effort to test it, and it only contains straightforward wiring code.

# Implementation notes

## CharacterReaderInputStream

`CharacterReaderInputStream` is used to ingest elements from `CharacterReader` into an Akka stream.

Since Akka already contains `InputStream` to `Source` adapter and `CharacterReader` resembles
`InputStream` closely, I decided to make `CharacterReader` to `InputSource` adapter.

The whole conversion scheme looks like:

`CharacterReader` -> `CharacterReaderInputStream` -> `InputStreamSource` -> `Source`

## WordCount

`WordCount` contains stream building blocks that form the solution.

### .source

Two `.source` methods allow to get an Akka source given a `CharacterReader`. Resulting source emits
whole words. It is essential to combine characters into words before merging several sources into
one to avoid mingling characters from different sources.

Setting `chunkSize` to 1 is important. By default `InputStreamSource` tries to read 8K elements
before emitting anything.

### .flow

Contains the heart of the solution. `.scan` operator is used to fold over the stream and aggregate
words into a word-to-count `Map`.

`.conflate` operator is used to prevent slow downstream of backpressuring the upstream by dropping
old elements and keeping the last one.

`.extrapolate` operator repeats the last element to make it always available for consuming.

Lastly, the flow is zipped with a tick source, to only emit when tick is received.

### .sink

`.sink` formats the `Map` into a `String` and prints it. The formatting part is moved out of the
`.flow` to ease testing.