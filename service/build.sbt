lazy val appMainClass = Some("server.Http4sMongoServer")
mainClass in (Compile, run) := appMainClass
mainClass in (Compile, packageBin) := appMainClass