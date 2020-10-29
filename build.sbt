name := "jmap-validation"

version := "0.1"

scalaVersion := "2.13.3"

val zioVersion =  "1.0.2"

val testcontainersScalaVersion =  "0.38.4"
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client" %% "core" % "2.2.9",
  "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.2.9",
  "commons-net" % "commons-net" % "3.7.1",
  "dev.zio" %% "zio"          % zioVersion,
  "dev.zio" %% "zio-json" % "0.0.0+28-e548a5ac-SNAPSHOT",
  "dev.zio" %% "zio-test"          % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt"      % zioVersion % "test",
  "dev.zio" %% "zio-test-magnolia" % zioVersion % "test", // optional
  "com.dimafeng" %% "testcontainers-scala-core" % testcontainersScalaVersion % "test",
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")