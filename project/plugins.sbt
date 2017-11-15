lazy val sbtAssembly = "com.eed3si9n" % "sbt-assembly" % "0.14.5"
lazy val sbtDocker: ModuleID = "se.marcuslonnberg" % "sbt-docker" % "1.5.0"

addSbtPlugin(sbtAssembly)
addSbtPlugin(sbtDocker)