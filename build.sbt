val scala3Version = "3.7.1"
val zioVersion = "2.1.19"

lazy val root = project
  .in(file("."))
  .settings(
    name := "split-it",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,

      // ZIO
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-http" % "3.0.1",
      "dev.zio" %% "zio-json" % "0.6.2",

      // Logging
      "dev.zio" %% "zio-logging" % "2.1.15",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.15",
      "org.slf4j" % "slf4j-simple" % "2.0.9",

      "com.auth0" % "java-jwt" % "4.5.0",
      "io.getquill" %% "quill-zio" % "4.8.6",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
      "com.h2database" % "h2" % "2.2.224",
    )
  )
