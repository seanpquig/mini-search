name := "MiniSearch"

version := "1.0"

scalaVersion := "2.12.2"

val akkaHttpVersion = "10.0.9"

fork := true

javaOptions in run += "-Dconfig.file=config/application.conf"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1",
  "org.rocksdb" % "rocksdbjni" % "5.7.3"
)
