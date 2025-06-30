val scala3Version = "3.7.1"
val zioVersion = "2.1.19"

lazy val root = project
  .in(file("."))
  .settings(
    name := "split-it",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", xs@_*) => MergeStrategy.concat
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assembly / mainClass := Some("com.github.ai.split.Main"),

    libraryDependencies ++= Seq(
      // Testing
      "org.scalameta" %% "munit" % "1.0.0" % Test,

      // ZIO
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.1",
      "dev.zio" %% "zio-json" % "0.6.2",

      // Logging
      "dev.zio" %% "zio-logging" % "2.3.2",
      "dev.zio" %% "zio-logging-slf4j" % "2.3.1",
      "ch.qos.logback" % "logback-classic" % "1.5.11",

      // JWT
      "com.auth0" % "java-jwt" % "4.5.0",

      // Database
      "io.getquill" %% "quill-zio" % "4.8.6",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
      "com.h2database" % "h2" % "2.3.232",
    )
  )
