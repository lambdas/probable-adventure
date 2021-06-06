package lambdas.challenge

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters
import akka.stream.scaladsl.Framing
import akka.util.ByteString
import akka.stream.scaladsl.Merge

object WordCount {

  def source(readers: Seq[CharacterReader]): Source[String, NotUsed] = {
    Source
      .combine(Source.empty, Source.empty, readers.map(source): _*)(Merge(_)) // Yes, this API is weird..
  }

  def source(reader: CharacterReader): Source[String, NotUsed] = {
    StreamConverters
      .fromInputStream(() => new CharacterReaderInputStream(reader), chunkSize = 1)
      .via(Framing.delimiter(ByteString(" "), maximumFrameLength = 256, allowTruncation = true))
      .map(_.utf8String)
      .mapMaterializedValue(_ => NotUsed)
  }

  def flow(tickSource: Source[_, _]): Flow[String, Map[String, Int], NotUsed] = {
    Flow[String]
      .map(_.toLowerCase)
      .scan(Map.empty[String, Int])((acc, s) => acc.updated(s, acc.getOrElse(s, 0) + 1))
      .conflate((_, elem) => elem)
      .extrapolate(Iterator.continually(_), None)
      .zipWith(tickSource)((elem, _) => elem)
  }
}