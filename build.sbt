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

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
   "org.zeromq" % "jeromq" % "0.3.0"
)
