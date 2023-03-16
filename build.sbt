ThisBuild / organization := "ch"

ThisBuild / scalaVersion := "2.13.10"

val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"

lazy val root = (project in file("."))
  /* .enablePlugins(SbtPlugin) */
  .enablePlugins(UniversalPlugin)
  .settings(
    name := "Course-Description-Automation",
    //assembly / assemblyJarName := name + ".jar"
    /* scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    }, */
    /* scriptedLaunchOpts ++= List("-Dfile.encoding=UTF-8"), */
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    libraryDependencies ++= Seq(prettyPrintJsonLib, jsonLib)
  )


