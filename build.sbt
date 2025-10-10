import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.17"

lazy val root = (project in file("."))
  .settings(
    name := "da-reference-generator",
    libraryDependencies ++= Seq(
      awsSdkDynamoDbV2,
      awsSdkDynamoDbV1 % Test,
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
      testContainer % Test,
      testContainerDynalite % Test
    )
  )

dependencyOverrides += "commons-logging" % "commons-logging" % "1.3.5"

(assembly / assemblyJarName) := "reference-generator.jar"

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
