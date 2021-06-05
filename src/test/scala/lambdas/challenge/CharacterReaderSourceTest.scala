package lambdas.challenge

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.EOFException
import scala.concurrent.Await
import scala.concurrent.duration._

class CharacterReaderSourceTest 
  extends AnyFlatSpec 
  with BeforeAndAfterAll 
  with Matchers 
  with MockFactory
  with ScalaFutures {

  private implicit val system: ActorSystem = ActorSystem("WordCountFlowTest")

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 1.minute)
  }

  "CharacterReaderSource" should "call nextCharacter() to get the next element" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().returns('a').once()
    (reader.nextCharacter _).when().throws(new EOFException)

    CharacterReaderSource(reader).runWith(Sink.seq).futureValue shouldBe List("a")
  }

  it should "close the stream when CharacterReader throws EOFException" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)

    CharacterReaderSource(reader).runWith(Sink.seq).futureValue shouldBe Nil
  }

  it should "close CharacterReader when the stream is closed" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)
    (reader.close _).when().returns().once()

    CharacterReaderSource(reader).runWith(Sink.seq).futureValue
  }
}
