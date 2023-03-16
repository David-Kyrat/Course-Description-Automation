ThisBuild / organization := "ch"

ThisBuild / scalaVersion := "2.13.10"

val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"

enablePlugins(UniversalPlugin, JavaAppPackaging, WindowsPlugin)

lazy val root = (project in file(".")).settings(
  name := "Course-Description-Automation",
  version := "0.1",
  assembly / assemblyJarName := "cool.jar", // name + ".jar",
  /* libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1", */
  libraryDependencies ++= Seq(scalaBaseDep, prettyPrintJsonLib, jsonLib),
  maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
  packageSummary := "test-windows",
  packageDescription := """Test Windows MSI."""
)

Windows / mappings := (Universal / mappings).value

Windows / mappings ++= {
    val jar = (Compile / packageBin).value
    val dir = (Windows / sourceDirectory).value
    Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
}

/* Windows / mappings ++= jar.zipWith(dir) */

/* mappings in Windows ++= (packageBin in Compile, sourceDirectory in Windows) map { (jar, dir) =>
  Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
} */
/* Windows / mappings ++= (Compile / packageBin, Windows / sourceDirectory) map { (jar, dir) =>
    Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
} */
/*
Windows / mappings ++= (Compile / packageBin, Windows / sourceDirectory) map { (jar, dir) =>
    Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
}

wixFeatures += WindowsFeature(
  id = "BinaryAndPath",
  title = "My Project's Binaries and updated PATH settings",
  desc = "Update PATH environment variables (requires restart).",
  components = Seq(ComponentFile("bin/cool.bat"), ComponentFile("lib/cool.jar"), AddDirectoryToPath("bin"))
) */
