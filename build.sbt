import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .settings(
    name := "da-reference-generator",
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
    libraryDependencies ++= Seq(
      awsSdkDynamoDbV2,
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
      testContainerLocalstack % Test
    )
  )

dependencyOverrides += "commons-logging" % "commons-logging" % "1.4.0"

(assembly / assemblyJarName) := "reference-generator.jar"

(assembly / assemblyOutputPath) := Def.uncached{
  baseDirectory.value / "target" / "scala-2.13" / (assembly / assemblyJarName).value
}

(assembly / assemblyMergeStrategy) := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
