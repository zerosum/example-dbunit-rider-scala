name := "example-dbunit-rider-scala"

scalaVersion := "2.13.4"

libraryDependencies ++= {
  val ScalaTestVersion = "3.2.2"
  Seq(
    "org.scalatest"              %% "scalatest-core"                 % ScalaTestVersion % "test",
    "org.scalatest"              %% "scalatest-funspec"              % ScalaTestVersion % "test",
    "org.scalatest"              %% "scalatest-shouldmatchers"       % ScalaTestVersion % "test",
    "org.dbunit"                  % "dbunit"                         % "2.7.0"          % "test",
    "com.github.database-rider"   % "rider-core"                     % "1.21.1"         % "test",
    "com.dimafeng"               %% "testcontainers-scala-scalatest" % "0.38.8"         % "test",
    "org.testcontainers"          % "postgresql"                     % "1.15.1"         % "test",
    "ch.qos.logback"              % "logback-classic"                % "1.2.3"          % "test",
    "org.slf4j"                   % "slf4j-api"                      % "1.7.30"         % "test",
    "com.typesafe.scala-logging" %% "scala-logging"                  % "3.9.2"          % "test",
    "org.scala-sbt"              %% "io"                             % "1.4.0"          % "test"
  )
}

Test / fork := true
