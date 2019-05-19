name := "heatzy"

version := "0.1"

scalaVersion := "2.12.8"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val http4sVersion= "0.20.1"
libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.4"
libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.4"
libraryDependencies += "com.lihaoyi" %% "requests" % "0.1.8"
libraryDependencies += "org.http4s" %% "http4s-core" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-circe" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-client" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-dsl" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-prometheus-metrics" % http4sVersion
libraryDependencies += "io.circe" %% "circe-generic" % "0.11.1"
libraryDependencies += "io.circe" %% "circe-literal" % "0.11.1"
libraryDependencies += "org.scalaz" %% "scalaz-zio" % "1.0-RC4"
libraryDependencies += "org.scalaz" %% "scalaz-zio-interop-cats" % "1.0-RC4"

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.11.0"


scalacOptions ++= Seq("-Ypartial-unification")

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.0")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
