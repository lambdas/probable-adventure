package lambdas.challenge

import java.io.EOFException
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class SlowCharacterReader(contents: String,
                          minDelay: FiniteDuration,
                          maxDelay: FiniteDuration) extends CharacterReader {

  private val iterator = contents.iterator

  override def nextCharacter(): Char = {
    sleep()
    iterator.nextOption().getOrElse(throw new EOFException)
  }

  override def close(): Unit = {
    // Do nothing.
  }

  private def sleep(): Unit = {
    Thread.sleep(Random.between(minDelay.toMillis, maxDelay.toMillis))
  }
}
