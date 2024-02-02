import sbt._

object Dependencies {

  private val circeVersion = "0.14.6"
  private val testContainersVersion = "0.41.2"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.17"
  lazy val awsSdkDynamoDbV2 = "software.amazon.awssdk" % "dynamodb" % "2.23.14"
  lazy val awsSdkDynamoDbV1 = "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.12.648"
  lazy val lambdaJavaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.2.3"
  lazy val lambdaJavaEvents = "com.amazonaws" % "aws-lambda-java-events" % "3.11.4"
  lazy val ocitools = "uk.gov.nationalarchives.oci" % "oci-tools-scala_2.13" % "0.4.0"
  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.4.14"
  lazy val logstash = "net.logstash.logback" % "logstash-logback-encoder" % "7.4"
  lazy val typesafe = "com.typesafe" % "config" % "1.4.3"
  lazy val testContainer = "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion
  lazy val testContainerDynalite = "com.dimafeng" %% "testcontainers-scala-dynalite" % testContainersVersion
}
