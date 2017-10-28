import Dependencies._

name := "http4s-mongo"

lazy val common = (project in file("common"))
  .settings(
    Commons.settings,
    libraryDependencies ++= commonDependencies
  )

lazy val model = (project in file("model"))
    .settings(
      Commons.settings,
      libraryDependencies ++= modelDependencies
    ).dependsOn(common)

lazy val core = (project in file("core"))
  .settings(
    Commons.settings,
    libraryDependencies ++= coreDependencies
  ).dependsOn(model)

lazy val service = (project in file("service"))
    .settings(
      Commons.settings,
      libraryDependencies ++= serviceDependencies,
      mainClass in assembly := Some("server.Http4sServer")
    ).dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(common, model, core, service)
  .settings(
    Commons.settings
  )