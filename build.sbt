// Project name (artifact name in Maven)
name := "Atomicommit"

// orgnization name (e.g., the package name of the project)
organization := "atomicommit"

version := "1.0-SNAPSHOT"

// project description
description := "Atomic Commit Project"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

javacOptions += "-Xlint:unchecked"

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
   "org.zeromq" % "jeromq" % "0.3.0",
   "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
   "org.apache.logging.log4j" % "log4j-core" % "2.8.2"
)

//javacOptions in (Compile,doc) ++= Seq("-doclet", "info.leadinglight.umljavadoclet.UmlJavaDoclet", "-docletpath", "src/main/resources/uml-java-doclet-1.0-SNAPSHOT.jar")
javacOptions in (Compile,doc) ++= Seq(
  "-doclet", "nl.talsmasoftware.umldoclet.UMLDoclet",
  "-docletpath", "lib/umldoclet-1.0.9.jar",
  "-umlIncludePrivateFields", "true"
)
