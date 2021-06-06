package lambdas.challenge

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source

object WordCount {

  def flow(tickSource: Source[_, _]): Flow[String, Map[String, Int], NotUsed] = {
    Flow[String]
      .map(_.toLowerCase)
      .scan(Map.empty[String, Int])((acc, s) => acc.updated(s, acc.getOrElse(s, 0) + 1))
      .conflate((_, elem) => elem)
      .extrapolate(Iterator.continually(_), None)
      .zipWith(tickSource)((elem, _) => elem)
  }
}