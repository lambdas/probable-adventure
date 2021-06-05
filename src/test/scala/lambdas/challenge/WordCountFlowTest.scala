package lambdas.challenge

import org.scalatest.flatspec.AnyFlatSpec
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import org.scalatest.concurrent.Futures._
import org.scalatest.flatspec.AnyFlatSpecLike
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.concurrent.Futures
import org.scalatest.matchers.should.Matchers
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures

class WordCountFlowTest extends AnyFlatSpec with BeforeAndAfterAll with ScalaFutures with Matchers {

  private implicit val system: ActorSystem = ActorSystem("WordCountFlowTest")

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 1.minute)
  }

  "WordCountFlow" should "respond with an empty map when completed" in {
    val (_, result) = WordCountFlow().runWith(Source.empty, Sink.last)
    result.futureValue shouldBe Map.empty
  }

  it should "respond with word count when completed" in {
    val (_, result) = WordCountFlow().runWith(Source(List("the", "cat", "sat", "on", "the", "mat")), Sink.last)
    result.futureValue shouldBe Map("the" -> 2, "cat" -> 1, "sat" -> 1, "on" -> 1, "mat" -> 1)
  }
}