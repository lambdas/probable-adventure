package lambdas.challenge

import java.io.EOFException
import java.io.InputStream

/** An adapter to turn a [[lambdas.challenge.CharacterReader]] into an [[java.io.InputStream]]. */
class CharacterReaderInputStream(reader: CharacterReader) extends InputStream {

  override def read(): Int = {
    try {
      reader.nextCharacter().toInt
    } catch {
      case _: EOFException => -1
    }
  }

  override def close(): Unit = {
    reader.close()
  }
}
