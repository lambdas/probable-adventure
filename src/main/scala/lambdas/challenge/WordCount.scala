package lambdas.challenge

import akka.Done
import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Merge
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.chaining._

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
      .map(_.replaceAll("[,.!]", "").toLowerCase)
      .scan(Map.empty[String, Int])((acc, s) => acc.updated(s, acc.getOrElse(s, 0) + 1))
      .conflate((_, elem) => elem)
      .extrapolate(Iterator.continually(_), None)
      .zipWith(tickSource)((elem, _) => elem)
  }

  def sink: Sink[Map[String, Int], Future[Done]] = {
    Sink.foreach(println).contramap(format)
  }

  def format(wordCount: Map[String, Int]): String = {
    wordCount
      .toList
      .sortBy(_._1)
      .map { case (k, v) => s"$k:\t$v" }
      .pipe(xs => if (xs.isEmpty) List("empty") else xs)
      .mkString("Word count:\n  ", "\n  ", "\n")
  }
}