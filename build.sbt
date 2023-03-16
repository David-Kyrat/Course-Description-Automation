
// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.10"
// That is, to create a valid sbt build, all you've got to do is define the
// version of Scala you'd like your project to use.

// ============================================================================

// Lines like the above defining `scalaVersion` are called "settings". Settings
// are key/value pairs. In the case of `scalaVersion`, the key is "scalaVersion"
// and the value is "2.13.8"

// It's possible to define many kinds of settings, such as:

name := "Course-Description-Automation"
organization := "ch"

// Packaging Config Infos
enablePlugins(UniversalPlugin)
enablePlugins(WindowsPlugin)

maintainer := "Noah Munz <munz.no@gmail.com>"
packageSummary := "test-windows"
packageDescription := """Test Windows MSI."""

// wix build information
wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"
wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"

// customize:

/* mappings in Windows ++= (packageBin in Compile, sourceDirectory in Windows) map { (jar, dir) =>
  Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
} */

/* wixFeatures += WindowsFeature(
    id="BinaryAndPath",
    title="Project's Binaries and updated PATH settings",
    desc="Update PATH environment variables (requires restart).",
    components = Seq(
      ComponentFile("bin/cool.bat"),
      ComponentFile("lib/cool.jar"),
      AddDirectoryToPath("bin"))
) */

// Want to use a published library in your project?
// You can define other libraries as dependencies in your build like this:

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.6"
libraryDependencies += "com.google.code.gson" % "gson" % "2.10.1"

// Here, `libraryDependencies` is a set of dependencies, and by using `+=`,
// we're adding the scala-parser-combinators dependency to the set of dependencies

scalacOptions += "-deprecation"

