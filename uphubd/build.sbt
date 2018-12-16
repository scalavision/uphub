name := "uphubd"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"
scalacOptions += "-language:higherKinds"

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

libraryDependencies += "org.dispatchhttp" %% "dispatch-core" % "1.0.0"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.27"
libraryDependencies += "org.scalaz" %% "scalaz-deriving" % "1.0.0"
libraryDependencies += "org.scalaz" %% "scalaz-zio" % "0.5.1"
libraryDependencies += "org.scalaz" %% "scalaz-zio-interop" % "0.5.0"
