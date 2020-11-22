scalaVersion := "2.13.4"

name := "mondrian_poc"
organization := "fr.chris-andre"
version := "0.1"

resolvers += Resolver.mavenLocal

val typesafeConfig_version = "1.4.0"
val zio_version            = "1.0.3"
val mondrian_version       = "9.2.0.0-SNAPSHOT"
val h2_version             = "1.4.200"

libraryDependencies ++= List(
  //  "dev.zio"       %% "zio"              % zio,
  "com.typesafe"   % "config"   % typesafeConfig_version,
  "pentaho"        % "mondrian" % mondrian_version exclude("xerces", "xercesImpl"),
  "com.h2database" % "h2"       % h2_version,
  "xml-apis"       % "xml-apis" % "1.4.01"
)

enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)
dockerBaseImage := "openjdk:8-jre-slim-buster"
dockerUpdateLatest := true
