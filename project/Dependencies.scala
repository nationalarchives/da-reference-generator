import sbt.*

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.16"
  lazy val awsSdkDynamoDb = "software.amazon.awssdk" % "dynamodb" % "2.20.110"
}
