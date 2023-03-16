ThisBuild / organization := "ch"

ThisBuild / scalaVersion := "2.13.10"

val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"

lazy val root = (project in file("."))
    .enablePlugins(UniversalPlugin, JavaAppPackaging, WindowsPlugin)
    .settings(
      name := "Course-Description-Automation",
      assembly / assemblyJarName := name + ".jar",
      libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
      libraryDependencies ++= Seq(prettyPrintJsonLib, jsonLib),
      maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
      packageSummary := "test-windows",
      packageDescription := """Test Windows MSI."""
    )

/* Windows / mappings := (Universal / mappings).value
Windows / mappings ++= (Compile / packageBin, Windows / sourceDirectory) map { (jar, dir) =>
    Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
}

wixFeatures += WindowsFeature(
  id = "BinaryAndPath",
  title = "My Project's Binaries and updated PATH settings",
  desc = "Update PATH environment variables (requires restart).",
  components = Seq(ComponentFile("bin/cool.bat"), ComponentFile("lib/cool.jar"), AddDirectoryToPath("bin"))
) */
