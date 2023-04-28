
// Global / onChangedBuildSource := ReloadOnSourceChanges

// NB:  ----------------------------- BUILD INFOS ---------------------------

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "ch"


// NB:  ----------------------------- PATHS --------------------------------

val resDir_String = "res"
val resDir_File = file(resDir_String) // File object that can be passed to functions that doesnt accept macros like `resourceDirectory`
resourceDirectory := baseDirectory.value / resDir_String

Compile / resourceDirectory := resourceDirectory.value

val pName = "Course-Description-Automation" // project Name
val pNameLower = pName.toLowerCase

// val jarName = pNameLower + ".jar"
// val jarPath = "lib/" + jarName

val batName = pNameLower + ".bat"
val batPath = "bin/" + batName

publishArtifact := false

// NB:  ----------------------------- DEPENDENCIES  -------------------------

val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"
val parallelCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"

// enablePlugins(UniversalPlugin, JavaAppPackaging, WindowsPlugin)

// NB: ----------------------------- PROJECT DEF ----------------------------

lazy val root = (project in file(".")).settings(
  name := pName,
  version := "0.1",
  resourceDirectory := baseDirectory.value / resDir_String,
  libraryDependencies ++= Seq( scalaBaseDep, prettyPrintJsonLib, jsonLib, parallelCollections),
  publish := {}
)
// maintainer := "Noah Munz <munz.no@gmail.com>",
// packageSummary := "Course-Description-Automation Installer",
// packageDescription := """Application to automatically generate printable 1-2 page PDF of course descriptions.""",
