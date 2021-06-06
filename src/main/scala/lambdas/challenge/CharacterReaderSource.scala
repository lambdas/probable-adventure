package lambdas.challenge

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString

import scala.concurrent.Future

/** An adapter to turn a [[lambdas.challenge.CharacterReader]] into an [[akka.stream.scaladsl.Source]]. */
object CharacterReaderSource {

  def apply(reader: CharacterReader): Source[String, Future[IOResult]] = {
    StreamConverters
      .fromInputStream(() => new CharacterReaderInputStream(reader), chunkSize = 1)
      .via(Framing.delimiter(ByteString(" "), maximumFrameLength = 256, allowTruncation = true))
      .map(_.utf8String)
  }
}
