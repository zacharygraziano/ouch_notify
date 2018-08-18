package tech.dougie.ouch

import java.net.URL

import com.amazonaws.services.cognitoidp.model.ListUsersInGroupRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.twilio.`type`.PhoneNumber
import com.twilio.rest.api.v2010.account.Message
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class SMSNotifier(implicit config: Config, ec: ExecutionContext) extends LazyLogging {

  private def cognitoPhoneNumbers: Future[Seq[String]] = {
    val PhoneNumber = "phone_number"
    val request = new ListUsersInGroupRequest()
      .withGroupName(config.cognitoConfig.userPoolGroup)
      .withUserPoolId(config.cognitoConfig.userPoolId)
    Future {
      config.cognitoConfig.cognitoClient
        .listUsersInGroup(request)
        .getUsers
        .asScala
        .flatMap(
          _.getAttributes.asScala
            .find(_.getName == PhoneNumber)
            .map(_.getValue))
    }
  }

  private def nSecondsFromNow(n: Long): java.util.Date = {
    val expiration = new java.util.Date()
    val expirationInMillis = expiration.getTime() + 1000L * n
    expiration.setTime(expirationInMillis)
    expiration
  }

  def getPresignedS3Url(s3Key: String): Future[URL] = {
    val request = new GeneratePresignedUrlRequest(
      config.s3Config.bucket,
      s3Key
    ).withExpiration(nSecondsFromNow(180L))
    config.s3Config.s3Client.generatePresignedUrlRequest(request)
  }

  def sendMessages(
    messageBody: String,
    url: URL,
    phoneNumbers: Seq[String]): Future[Seq[Message]] = {
    val mediaList = new java.util.ArrayList[java.net.URI](1)
    mediaList.add(url.toURI)
    Future.sequence(phoneNumbers.map { receiving =>
      Future {
        Message
          .creator(
            new PhoneNumber(receiving),
            config.twilioConfig.outgoingPhone,
            mediaList
          )
          .setBody(messageBody)
          .create(config.twilioConfig.twilioClient)
      }
    })
  }

  def randomMessage(): String = {
    config.messages(scala.util.Random.nextInt(config.messages.length))
  }

  def notify(mediaS3Path: String): Future[Seq[Message]] =
    for {
      phoneNumbers <- cognitoPhoneNumbers
      presignedUrl <- getPresignedS3Url(mediaS3Path)
      messages <- sendMessages(randomMessage(), presignedUrl, phoneNumbers)
    } yield messages
}
