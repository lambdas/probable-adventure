package lambdas.challenge

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.concurrent.Eventually
import java.io.EOFException
import scala.concurrent.duration._
import org.scalatest.matchers.should.Matchers

class SlowCharacterReaderTest
  extends AnyFlatSpec
  with Eventually
  with Matchers {

  "SlowCharacterReader" should "return characters from the passed string" in {
    val reader = new SlowCharacterReader("the cat sat on the mat", 10.millis, 100.millis)

    eventually { reader.nextCharacter() shouldBe 't' }
    eventually { reader.nextCharacter() shouldBe 'h' }
    eventually { reader.nextCharacter() shouldBe 'e' }
  }

  it should "throw EOFException when the string end is reached" in {
    val reader = new SlowCharacterReader("", 10.millis, 100.millis)

    eventually { an[EOFException] should be thrownBy reader.nextCharacter() }
  }
}
