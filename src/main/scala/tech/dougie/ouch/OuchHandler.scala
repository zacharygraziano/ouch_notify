package tech.dougie.ouch

import com.amazonaws.services.lambda.runtime.events.S3Event

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class OuchHandler {
  def keysFromEvent(event: S3Event): Seq[String] =
    event.getRecords.asScala.map(_.getS3.getObject.getKey).filter(_.endsWith(".gif"))

  def handle(event: S3Event): Unit = {
    implicit val config: Config = Config.load()
    val keys = keysFromEvent(event)
    val notifier = new SMSNotifier

    val notifications = Future.sequence(keys.map(key => notifier.notify(key)))
    notifications onComplete { _ =>
      config.s3Config.s3Client.shutdown()
    }

    Await.result(notifications, 2 minutes)
  }
}
