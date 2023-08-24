import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "da-reference-generator"
  )

libraryDependencies ++= Seq(
  awsSdkDynamoDb,
  lambdaJavaCore,
  lambdaJavaEvents,
  scalaTest % Test,
  mockito % Test,
  ocitools,
  circeCore,
  circeGeneric,
  circeParser,
  scalaLogging,
  logback,
  logstash,
  typesafe
)

(assembly / assemblyJarName) := "reference-generator.jar"

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", xs @_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
