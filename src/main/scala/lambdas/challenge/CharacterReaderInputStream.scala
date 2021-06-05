package lambdas.challenge

import java.io.InputStream
import java.io.EOFException

/** An adapter to turn a [[lambdas.challenge.CharacterReader]] into an [[java.io.InputStream]]. */
class CharacterReaderInputStream(characterReader: CharacterReader) extends InputStream {

  override def read(): Int = {
    try {
      characterReader.nextCharacter()
    } catch {
      case _: EOFException => -1
    }
  }

  override def close(): Unit = {
    characterReader.close()
  }
}