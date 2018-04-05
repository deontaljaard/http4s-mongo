import sbt._

object Dependencies {
  val catsCoreVersion = "1.1.0"
  val catsEffectVersion = "0.10"
  val fs2IOVersion = "0.10.3"
  val http4sVersion = "0.18.7"
  val circeVersion = "0.9.2"
  val scalaLoggingVersion = "3.8.0"
  val logbackClassicVersion = "1.2.3"
  val mongoScalaVersion = "2.1.0"
  val reactiveMongoScalaVersion = "0.13.0"
  val mongoJavaVersion = "3.4.2"
  val nettyVersion = "4.1.22.Final"
  val specs2Version = "4.0.2"
  val jwtVersion = "1.2.2"
  val awsS3SdkVersion = "1.11.301"

  // Compile Dependencies
  val catsCore: ModuleID = "org.typelevel" %% "cats-core" % catsCoreVersion
  val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % catsEffectVersion
  val fs2IO: ModuleID = "co.fs2" %% "fs2-io" % fs2IOVersion

  val http4sBlazeServer: ModuleID = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  val http4sCirce: ModuleID       = "org.http4s" %% "http4s-circe" % http4sVersion
  val http4sDsl: ModuleID         = "org.http4s" %% "http4s-dsl" % http4sVersion

  val circeCore: ModuleID     = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric: ModuleID  = "io.circe" %% "circe-generic" % circeVersion
  val circeParser: ModuleID   = "io.circe" %% "circe-parser" % circeVersion

  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
  val logbackCore: ModuleID     = "ch.qos.logback" % "logback-core" % logbackClassicVersion
  val logbackClassic: ModuleID  = "ch.qos.logback" % "logback-classic" % logbackClassicVersion

  val mongoScalaDriver: ModuleID    = "org.mongodb.scala" %% "mongo-scala-driver" % mongoScalaVersion
  val mongoScalaBson: ModuleID      = "org.mongodb.scala" %% "mongo-scala-bson" % mongoScalaVersion
  val mongoBson: ModuleID           = "org.mongodb" % "bson" % mongoJavaVersion
  val mongoDriverCore: ModuleID     = "org.mongodb" % "mongodb-driver-core" % mongoJavaVersion
  val mongoDriverAsync: ModuleID    = "org.mongodb" % "mongodb-driver-async" % mongoJavaVersion
  val reactiveMongoScala: ModuleID  = "org.reactivemongo" %% "reactivemongo" % reactiveMongoScalaVersion

  val nettyCommon: ModuleID     = "io.netty" % "netty-common" % nettyVersion
  val nettyTransport: ModuleID  = "io.netty" % "netty-transport" % nettyVersion
  val nettyBuffer: ModuleID     = "io.netty" % "netty-buffer" % nettyVersion

  val iglJwt: ModuleID = "io.igl" %% "jwt" % jwtVersion
  val awsS3Sdk: ModuleID = "com.amazonaws" % "aws-java-sdk-s3" % awsS3SdkVersion

  // Test dependencies
  val specs2Core: ModuleID = "org.specs2" %% "specs2-core" % specs2Version
  val specs2Mock: ModuleID = "org.specs2" %% "specs2-mock" % specs2Version

  // Module dependencies
  lazy val commonDependencies: Seq[ModuleID] = Seq(
    catsCore,
    catsEffect,
    fs2IO,
    scalaLogging,
    logbackCore,
    logbackClassic,
    circeCore,
    circeGeneric,
    circeParser,
    specs2Core % Test,
    specs2Mock % Test
  )

  lazy val modelDependencies: Seq[ModuleID] = Seq(
    mongoScalaDriver,
    mongoScalaBson,
    mongoBson,
    mongoDriverCore,
    mongoDriverAsync,
    reactiveMongoScala,
    awsS3Sdk
  ) ++ commonDependencies

  lazy val coreDependencies: Seq[ModuleID] = Nil ++ commonDependencies

  lazy val serviceDependencies: Seq[ModuleID] = Seq(
    http4sBlazeServer,
    http4sCirce,
    http4sDsl,
    iglJwt
  ) ++ commonDependencies

}
