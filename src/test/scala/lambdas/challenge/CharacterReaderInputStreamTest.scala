package lambdas.challenge

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.EOFException

class CharacterReaderInputStreamTest extends AnyFlatSpec with Matchers with MockFactory {

  "CharacterReaderInputStream" should "call nextCharacter() to get the next element" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().returns('a').once()

    new CharacterReaderInputStream(reader).read() shouldBe 'a'
  }

  it should "return -1 when CharacterReader throws EOFException" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)

    new CharacterReaderInputStream(reader).read() shouldBe -1
  }

  it should "close CharacterReader when closed" in {
    val reader = stub[CharacterReader]
    (reader.close _).when().returns(()).once()

    new CharacterReaderInputStream(reader).close()
  }
}