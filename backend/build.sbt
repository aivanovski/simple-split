val scala3Version = "3.7.1"
val zioVersion = "2.1.19"
val zioJsonVersion = "0.6.2"
val circeVersion = "0.14.10"
val zioDirect = "1.0.0-RC7"
val zioHttp = "3.0.1"

ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0"

lazy val generateOpenApi = taskKey[File]("Generate the OpenAPI YAML schema")

lazy val api = project
  .in(file("api"))
  .settings(
    name := "simple-split-api",
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "." + artifact.extension
    },
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-schema" % "1.4.1"
    )
  )

lazy val openapiSchema = project
  .in(file("openapi-schema"))
  .dependsOn(api)
  .settings(
    name := "simple-split-openapi-schema",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % zioHttp,
      "org.yaml" % "snakeyaml" % "2.0",
      "org.scalameta" %% "munit" % "1.0.0" % Test
    ),
    generateOpenApi := {
      val output = baseDirectory.value / "openapi.yaml"
      val classpath = (Compile / fullClasspath).value.files
      val appRunner = (Compile / runner).value
      appRunner
        .run(
          "com.github.ai.split.openapi.GenerateOpenApi",
          classpath,
          Array(output.getAbsolutePath),
          streams.value.log
        )
        .get
      streams.value.log.info(s"Generated $output")
      output
    }
  )

lazy val app = project
  .in(file("app"))
  .dependsOn(api, openapiSchema)
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
  .dependsOn(api, openapiSchema)
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
