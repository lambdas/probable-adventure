package lambdas.challenge

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.testkit.scaladsl.TestSource
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalamock.scalatest.MockFactory
import java.io.EOFException

class WordCountTest
  extends AnyFlatSpec
  with BeforeAndAfterAll
  with ScalaFutures
  with Matchers
  with MockFactory
  with Eventually {

  private implicit val system: ActorSystem = ActorSystem("WordCountFlowTest")

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 1.minute)
  }

  "WordCount.source" should "call nextCharacter() to get the next element" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().returns('a').once()
    (reader.nextCharacter _).when().throws(new EOFException)

    WordCount.source(reader).runWith(Sink.seq).futureValue shouldBe List("a")
  }

  it should "close the stream when CharacterReader throws EOFException" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)

    WordCount.source(reader).runWith(Sink.seq).futureValue shouldBe Nil
  }

  it should "close CharacterReader when the stream is closed" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)
    (reader.close _).when().returns().once()

    WordCount.source(reader).runWith(Sink.seq).futureValue
  }

  it should "merge words from all readers" in {
    val reader1 = stub[CharacterReader]
    (reader1.nextCharacter _).when().returns('t').once()
    (reader1.nextCharacter _).when().returns('h').once()
    (reader1.nextCharacter _).when().returns('e').once()
    (reader1.nextCharacter _).when().throws(new EOFException).once()

    val reader2 = stub[CharacterReader]
    (reader2.nextCharacter _).when().returns('c').once()
    (reader2.nextCharacter _).when().returns('a').once()
    (reader2.nextCharacter _).when().returns('t').once()
    (reader2.nextCharacter _).when().throws(new EOFException).once()

    WordCount
      .source(List(reader1, reader2))
      .runWith(Sink.seq)
      .futureValue should contain theSameElementsAs List("the", "cat")
  }

  "WordCount.flow" should "respond with an empty map when completed" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source.empty
    val (_, result) = WordCount.flow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map.empty
  }

  it should "respond with word count when completed" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source(List("the", "cat", "sat", "on", "the", "mat"))
    val (_, result) = WordCount.flow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map("the" -> 2, "cat" -> 1, "sat" -> 1, "on" -> 1, "mat" -> 1)
  }

  it should "only issue a word count on tick" in {
    val (tickProbe, tickSource) = TestSource.probe[Unit].preMaterialize()
    val (sourceProbe, sinkProbe) = WordCount.flow(tickSource).runWith(TestSource.probe[String], TestSink.probe)

    sourceProbe.sendNext("the")
    sinkProbe.request(1).expectNoMessage()

    tickProbe.sendNext(())
    sinkProbe.expectNext()
  }

  it should "issue the current word count on tick" in {
    val (tickProbe, tickSource) = TestSource.probe[Unit].preMaterialize()
    val (sourceProbe, sinkProbe) = WordCount.flow(tickSource).runWith(TestSource.probe[String], TestSink.probe)

    sourceProbe.sendNext("the")
    sourceProbe.sendNext("cat")
    sourceProbe.sendNext("sat")

    eventually {
      tickProbe.sendNext(())
      sinkProbe.requestNext() shouldBe Map("the" -> 1, "cat" -> 1, "sat" -> 1)

      tickProbe.sendNext(())
      sinkProbe.requestNext() shouldBe Map("the" -> 1, "cat" -> 1, "sat" -> 1)
    }
  }

  it should "lowercase incoming words" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source(List("The", "Cat", "sAt", "oN", "The", "Mat"))
    val (_, result) = WordCount.flow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map("the" -> 2, "cat" -> 1, "sat" -> 1, "on" -> 1, "mat" -> 1)
  }

  it should "ignore punctuation" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source(List("the", "cat", "sat..", "on", "the", "mat!"))
    val (_, result) = WordCount.flow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map("the" -> 2, "cat" -> 1, "sat" -> 1, "on" -> 1, "mat" -> 1)
  }

  "WordCount.format" should "format a word count" in {
    WordCount.format(Map("the" -> 1, "cat" -> 2, "sat" -> 3)) shouldBe """Word count:
                                                                         |  cat:	2
                                                                         |  sat:	3
                                                                         |  the:	1
                                                                         |""".stripMargin
  }

  it should "tell if a word count is empty" in {
    WordCount.format(Map.empty) shouldBe """Word count:
                                           |  empty
                                           |""".stripMargin
  }
}