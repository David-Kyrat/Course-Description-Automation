import Dependencies._

ThisBuild / scalaVersion     := "2.12.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ch.main"
ThisBuild / organizationName := "main"


/* ThisBuild / evictionErrorLevel := Level.Info */
addDependencyTreePlugin
addSbtPlugin("nz.co.bottech" % "sbt-scala2plantuml" % "0.3.0")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / libraryDependencySchemes += "com.thesamet.scalapb" %% "scalapb-runtime_2.12" % VersionScheme.Always


val scalaMeta ="org.scalameta" %% "semanticdb" % "4.1.6" 
val scala2PlantUml = "nz.co.bottech" %% "scala2plantuml" % "0.3.0"
val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"


lazy val root = (project in file(".")) .settings(
    name := "plantuml-test",
    libraryDependencies += munit % Test,
    libraryDependencies ++= Seq(
        scalaMeta,
        prettyPrintJsonLib,
        jsonLib,
        scala2PlantUml
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
