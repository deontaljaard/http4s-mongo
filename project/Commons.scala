import sbt._
import Keys._

object Commons {
  val appVersion = "1.0"

  val settings: Seq[Def.Setting[_]] = Seq(
    organization := "deontaljaard.github.io",
    version := appVersion,
    scalaVersion := "2.12.3",
    resolvers := Resolvers.http4sMongoResolvers,
  )
}
