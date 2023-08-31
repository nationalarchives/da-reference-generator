import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "da-reference-generator"
  )

libraryDependencies ++= Seq(
  awsSdkDynamoDbV2,
  awsSdkDynamoDbV1,
  lambdaJavaCore,
  lambdaJavaEvents,
  scalaTest % Test,
  ocitools,
  circeCore,
  circeGeneric,
  circeParser,
  scalaLogging,
  logback,
  logstash,
  typesafe,
  testContainer,
  testContainerDynalite
)

(Test / envVars) := Map("AWS_ACCESS_KEY_ID" -> "accesskey", "AWS_SECRET_ACCESS_KEY" -> "secret")

(assembly / assemblyJarName) := "reference-generator.jar"

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", xs @_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
