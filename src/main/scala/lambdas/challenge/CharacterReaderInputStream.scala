package lambdas.challenge

import java.io.InputStream
import java.io.EOFException

/** An adapter to turn a [[lambdas.challenge.CharacterReader]] into an [[java.io.InputStream]]. */
class CharacterReaderInputStream(reader: CharacterReader) extends InputStream {

  override def read(): Int = {
    try {
      reader.nextCharacter()
    } catch {
      case _: EOFException => -1
    }
  }

  override def close(): Unit = {
    reader.close()
  }
}