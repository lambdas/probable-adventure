package lambdas.challenge

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import java.io.EOFException

class CharacterReaderInputStreamTest extends AnyFlatSpec with Matchers with MockFactory {

  "CharacterReaderInputStream" should "call nextCharacter() to get the next element" in {
    val reader = mock[CharacterReader]
    (reader.nextCharacter _).expects().returning('a').once()

    new CharacterReaderInputStream(reader).read() shouldBe 'a'
  }

  it should "return -1 when CharacterReader throws EOFException" in {
    val reader = stub[CharacterReader]
    (reader.nextCharacter _).when().throws(new EOFException)

    new CharacterReaderInputStream(reader).read() shouldBe -1
  }

  it should "close CharacterReader when closed" in {
    val reader = mock[CharacterReader]
    (reader.close _).expects().once()

    new CharacterReaderInputStream(reader).close()
  }
}