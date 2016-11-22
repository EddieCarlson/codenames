name := "Codenames"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.4.10",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
