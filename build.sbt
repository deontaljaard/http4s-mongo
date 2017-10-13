organization := "deontaljaard.github.io"
name := "http4s-mongo"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.12.2"

lazy val http4sVersion = "0.17.1"
lazy val circeVersion = "0.8.0"
lazy val lagbackClassVersion = "1.2.1"
lazy val mongoScalaVersion = "2.1.0"
lazy val reactiveMongoScalaVersion = "0.12.5"
lazy val mongoJavaVersion = "3.4.2"
lazy val nettyVersion = "4.1.15.Final"
lazy val specs2Version = "4.0.0"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "ch.qos.logback" % "logback-classic" % lagbackClassVersion,
  "org.mongodb.scala" %% "mongo-scala-driver" % mongoScalaVersion,
  "org.mongodb.scala" %% "mongo-scala-bson" % mongoScalaVersion,
  "org.mongodb" % "bson" % mongoJavaVersion,
  "org.mongodb" % "mongodb-driver-core" % mongoJavaVersion,
  "org.mongodb" % "mongodb-driver-async" % mongoJavaVersion,
  "org.reactivemongo" % "reactivemongo_2.12" % "0.12.5" % "provided",
  "io.netty" % "netty-common" % nettyVersion,
  "io.netty" % "netty-transport" % nettyVersion,
  "io.netty" % "netty-buffer" % nettyVersion,
  "org.specs2" % "specs2-core_2.12" % specs2Version % "test",
  "org.specs2" % "specs2-mock_2.12" % specs2Version % "test"
)
