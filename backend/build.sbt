val scala3Version = "3.7.1"
val zioVersion = "2.1.19"
val zioJsonVersion = "0.6.2"
val circeVersion = "0.14.10"
val zioDirect = "1.0.0-RC7"
val zioHttp = "3.0.1"
val gsonVersion = "2.11.0"

ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0"

lazy val api = project
  .in(file("api"))
  .settings(
    name := "simple-split-api",
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "." + artifact.extension
    },
    libraryDependencies ++= Seq(
      "com.google.code.gson" % "gson" % gsonVersion
    )
  )

lazy val app = project
  .in(file("app"))
  .dependsOn(api)
  .settings(
    name := "simple-split-app",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", xs@_*) => MergeStrategy.concat
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    },
    assembly / mainClass := Some("com.github.ai.split.Main"),
    assembly / assemblyJarName := "simple-split-backend.jar",

    libraryDependencies ++= Seq(
      // Testing
      "org.scalameta" %% "munit" % "1.0.0" % Test,

      // ZIO
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % zioHttp,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-direct" % zioDirect,
      "io.github.cdimascio" % "dotenv-java" % "3.2.0",

      // Logging
      "dev.zio" %% "zio-logging" % "2.3.2",
      "dev.zio" %% "zio-logging-slf4j" % "2.3.1",
      "ch.qos.logback" % "logback-classic" % "1.5.13",

      // JWT
      "com.auth0" % "java-jwt" % "4.5.0",

      // Database
      "com.typesafe.slick" %% "slick" % "3.6.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.6.1",
      "org.xerial" % "sqlite-jdbc" % "3.51.1.0",

      // Password Hashing
      "org.mindrot" % "jbcrypt" % "0.4",
    )
  )

lazy val apiClient = project
  .in(file("api-client"))
  .dependsOn(api)
  .settings(
    name := "simple-split-api-client",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    assembly / mainClass := Some("com.github.ai.split.client.ApiClientMain"),
    assembly / assemblyJarName := "simple-split-api-client.jar",

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-direct" % zioDirect,
      "dev.zio" %% "zio-http" % zioHttp
    )
  )
