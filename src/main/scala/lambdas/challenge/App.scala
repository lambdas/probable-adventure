package lambdas.challenge

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import sun.misc.Signal

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** The application launcher. */
object App {

  private implicit val system: ActorSystem = ActorSystem("challenge")
  private implicit val ec: ExecutionContext = system.dispatcher

  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    initShutdownHook()

    WordCount
      .source(readers())
      .via(WordCount.flow(tickSource()))
      .runWith(WordCount.sink)
      .andThen { case _ => system.terminate() }
  }

  private def readers(): List[SlowCharacterReader] = {
    val minDelay = FiniteDuration(config.getDuration("reader-delay.min").toMillis, TimeUnit.MILLISECONDS)
    val maxDelay = FiniteDuration(config.getDuration("reader-delay.max").toMillis, TimeUnit.MILLISECONDS)

    config
      .getStringList("readers")
      .asScala
      .map(new SlowCharacterReader(_, minDelay, maxDelay))
      .toList
  }

  private def tickSource(): Source[Unit, NotUsed] = {
    val interval = FiniteDuration(config.getDuration("report-interval").toMillis, TimeUnit.MILLISECONDS)

    Source
      .tick(interval, interval, ())
      .mapMaterializedValue(_ => NotUsed)
  }

  private def initShutdownHook(): Unit = {
    Signal
      .handle(new Signal("INT"), _ => Await.result(system.terminate(), 5.minutes))
  }
}