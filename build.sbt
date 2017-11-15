import Dependencies._

name := "http4s-mongo"

lazy val dockerSettings = Seq(
  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    new Dockerfile {
      from("openjdk")
      add(artifact, artifactTargetPath)
      expose(8080)
      entryPoint("java", "-jar", artifactTargetPath)
    }
  }
)

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
    mainClass in assembly := Some("server.Http4sServer"),
    parallelExecution in Test := false, // https://github.com/mockito/mockito/issues/1067
    dockerSettings
  ).dependsOn(core % "compile->compile;test->test")
  .enablePlugins(DockerPlugin)

lazy val root = (project in file("."))
  .aggregate(common, model, core, service)
  .settings(
    Commons.settings
  )

addCommandAlias("dockerize", ";clean;assembly;service/docker")