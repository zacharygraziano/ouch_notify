package tech.dougie.ouch

import com.amazonaws.services.cognitoidp.{
  AWSCognitoIdentityProvider,
  AWSCognitoIdentityProviderClient
}
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import com.twilio.Twilio
import com.twilio.`type`.PhoneNumber
import com.twilio.http.TwilioRestClient
import com.typesafe.config.ConfigFactory
import tech.dougie.ouch.Config._

import scala.collection.JavaConverters._

final case class Config(
  s3Config: S3Config,
  twilioConfig: TwilioConfig,
  cognitoConfig: CognitoConfig,
  messages: Seq[String]
)

object Config {
  private val config = ConfigFactory.load()
  private val ssmClient = AWSSimpleSystemsManagementClient.builder().build()

  private def loadParam(name: String, encrypted: Boolean = true): String = {
    val request = new GetParameterRequest()
      .withName(name)
      .withWithDecryption(encrypted)

    ssmClient.getParameter(request).getParameter.getValue
  }

  def load(): Config = Config(
    S3Config.load(),
    TwilioConfig.load(),
    CognitoConfig.load(),
    config.getStringList("messages").asScala
  )

  final case class S3Config(
    bucket: String,
    s3Client: AmazonS3ScalaClient
  )

  object S3Config {
    def load(): S3Config = S3Config(
      config.getString("s3Bucket"),
      new AmazonS3ScalaClient()
    )
  }

  final case class CognitoConfig(
    userPoolId: String,
    cognitoClient: AWSCognitoIdentityProvider
  )

  object CognitoConfig {
    def load(): CognitoConfig = {
      CognitoConfig(
        config.getString("userPoolId"),
        AWSCognitoIdentityProviderClient.builder().build()
      )
    }
  }

  final case class TwilioConfig(
    outgoingPhone: PhoneNumber,
    twilioClient: TwilioRestClient
  )

  object TwilioConfig {
    def load(): TwilioConfig = {
      val twilioSid = loadParam("/twilio/doink/sid", encrypted = false)
      val twilioApiKey = loadParam("/twilio/doink/apikey")

      Twilio.init(
        twilioSid,
        twilioApiKey
      )

      TwilioConfig(
        new PhoneNumber(config.getString("outgoingPhone")),
        Twilio.getRestClient
      )
    }
  }
}
