name := "ouch-notify"

val awsSdkVersion = "1.11.335"
val lambdaSdkVersion = "1.1.0"
lazy val notifier = project
  .in(file("."))
  .settings(
    version := "1.0",
    organization := "tech.dougie",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "com.github.dwhjames" %% "aws-wrap" % "0.12.1",
      "com.typesafe" % "config" % "1.3.3",
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-ssm" % awsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-cognitoidp" % awsSdkVersion,
      "com.amazonaws" % "aws-lambda-java-core" % lambdaSdkVersion,
      "com.amazonaws" % "aws-lambda-java-events" % lambdaSdkVersion,
      "com.twilio.sdk" % "twilio" % "7.21.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case _                             => MergeStrategy.first
    },
    resolvers ++= Seq(
      Resolver.bintrayRepo("mingchuno", "maven")
    )
  )
