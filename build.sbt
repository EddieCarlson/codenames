name := "Codenames"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

version := "1.0"

scalaVersion := "2.10.3"

val spray = "1.3.1"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "io.spray" % "spray-can" % spray,
  "io.spray" % "spray-routing" % spray,
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
