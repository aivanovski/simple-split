val scala3Version = "3.7.1"
val zioVersion = "2.1.19"
val zioJsonVersion = "0.6.2"

ThisBuild / scalaVersion := scala3Version
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val api = project
  .in(file("api"))
  .settings(
    name := "simple-split-api",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % zioJsonVersion
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

    libraryDependencies ++= Seq(
      // Testing
      "org.scalameta" %% "munit" % "1.0.0" % Test,

      // ZIO
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.1",
      "dev.zio" %% "zio-json" % zioJsonVersion,

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

      // Password Hashing
      "org.mindrot" % "jbcrypt" % "0.4",
    )
  )

lazy val generateKotlinClasses = taskKey[Unit]("Generate Kotlin API classes")

lazy val codegen = project
  .in(file("codegen"))
  .dependsOn(api)
  .settings(
    name := "simple-split-codegen",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion
    ),
    generateKotlinClasses := {
      (Compile / runMain).toTask(" com.github.ai.split.codegen.TranspilerMain api/src/main/scala ./../android/backend-api/src/main/kotlin").value
    },
  )