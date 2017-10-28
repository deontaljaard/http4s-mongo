lazy val appMainClass = Some("server.Http4sServer")
mainClass in (Compile, run) := appMainClass
mainClass in (Compile, packageBin) := appMainClass