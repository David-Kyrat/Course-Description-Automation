import Dependencies._

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ch"

logLevel := Level.Error
maxErrors := 2
triggeredMessage := Watched.clearWhenTriggered

// Compile / resourceDirectory := resourceDirectory.value

lazy val root = (project in file(".")).settings(
    name := "CourseDescriptionAutomation",
    libraryDependencies ++= all,
    //resourceDirectory := baseDirectory.value / "files" / resDir_String
    )

lazy val cl = taskKey[Unit]("A task that gets the res path")
cl := { println("\033c") }
