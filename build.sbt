lazy val challenge = project.in(file("."))
  .settings(
    name         := "challenge",
    version      := "1.0.0",
    scalaVersion := "2.13.6",
    libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-stream"         % "2.6.14",
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.14" % Test,
      "org.scalatest"     %% "scalatest"           % "3.2.9"  % Test,
      "org.scalamock"     %% "scalamock"           % "5.1.0"  % Test,
    )
  )