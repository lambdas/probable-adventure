package lambdas.challenge

import akka.stream.scaladsl.Flow
import akka.NotUsed

object WordCountFlow {

  def apply(): Flow[String, Map[String, Int], NotUsed] = {
    Flow[String]
      .fold(Map.empty[String, Int])((acc, s) => acc.updated(s, acc.getOrElse(s, 0) + 1))
  }
}