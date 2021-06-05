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

class WordCountFlowTest 
  extends AnyFlatSpec 
  with BeforeAndAfterAll 
  with ScalaFutures 
  with Matchers 
  with Eventually {

  private implicit val system: ActorSystem = ActorSystem("WordCountFlowTest")

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 1.minute)
  }

  "WordCountFlow" should "respond with an empty map when completed" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source.empty
    val (_, result) = WordCountFlow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map.empty
  }

  it should "respond with word count when completed" in {
    val tickSource = Source.fromIterator(() => Iterator.continually(()))
    val source = Source(List("the", "cat", "sat", "on", "the", "mat"))
    val (_, result) = WordCountFlow(tickSource).runWith(source, Sink.last)

    result.futureValue shouldBe Map("the" -> 2, "cat" -> 1, "sat" -> 1, "on" -> 1, "mat" -> 1)
  }

  it should "only issue a word count on tick" in {
    val (tickProbe, tickSource) = TestSource.probe[Unit].preMaterialize()
    val (sourceProbe, sinkProbe) = WordCountFlow(tickSource).runWith(TestSource.probe[String], TestSink.probe)

    sourceProbe.sendNext("the")
    sinkProbe.request(1).expectNoMessage()

    tickProbe.sendNext(())
    sinkProbe.expectNext()
  }

  it should "issue the current word count on tick" in {
    val (tickProbe, tickSource) = TestSource.probe[Unit].preMaterialize()
    val (sourceProbe, sinkProbe) = WordCountFlow(tickSource).runWith(TestSource.probe[String], TestSink.probe)

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
}