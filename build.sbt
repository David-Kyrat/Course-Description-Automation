import sbt.IO
import com.typesafe.sbt.packager.windows.WindowsFeature
import com.typesafe.sbt.packager.windows._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.Keys.wixFeatures
import com.typesafe.sbt.packager.Keys.{wixProductId, wixProductUpgradeId}
import com.typesafe.sbt.packager.windows.WixHelper.generateComponentsAndDirectoryXml
import com.typesafe.sbt.packager.windows.WixHelper.{makeWixConfig, makeWixProductConfig, makeIdFromFile}
import com.typesafe.sbt.packager.Keys.{wixFile, wixFiles}
import Path.relativeTo

ThisBuild / organization := "ch"

ThisBuild / scalaVersion := "2.13.10"

val resDir_String = "res"
val resDir_File = file(resDir_String) // File object that can be passed to functions that doesnt accept macros like `resourceDirectory`
resourceDirectory := baseDirectory.value / resDir_String

Compile / resourceDirectory := resourceDirectory.value

val pName = "Course-Description-Automation" // project Name
val pNameLower = pName.toLowerCase

val jarName = pNameLower + ".jar"
val jarPath = "lib/" + jarName

val batName = pNameLower + ".bat"
val batPath = "bin/" + batName

val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"
val spdf = "io.github.cloudify" %% "spdf" % "1.4.0"

enablePlugins(UniversalPlugin, JavaAppPackaging, WindowsPlugin)

lazy val root = (project in file(".")).settings(
  name := pName,
  version := "0.1",
  resourceDirectory := baseDirectory.value / resDir_String,
  assembly / assemblyJarName := jarName, // name + ".jar",
  libraryDependencies ++= Seq(scalaBaseDep, prettyPrintJsonLib, jsonLib, spdf),
  maintainer := "Noah Munz <munz.no@gmail.com>",
  packageSummary := "Course-Description-Automation Installer",
  packageDescription := """MSI Installer for the application Course-Description-Automation""",
  // wix build information
  wixProductId := "ce07be71-510d-414a-92d4-dff47631848a",
  wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"
)

Windows / mappings := (Universal / mappings).value
//val resDirectory = resourceDirectory.value //file("/res")

Windows / mappings ++= {
    val jar = (Compile / packageBin).value
    /* val dir = (Windows / sourceDirectory).value */
    Seq(jar -> jarPath) // , (Compile / resourceDirectory).value ->  // , (dir / batName) -> batPath)
}

val comp = generateComponentsAndDirectoryXml(resDir_File, "res")

wixFeatures += WindowsFeature(
  id = "BinaryAndPath",
  // title = "My Project's Binaries and updated PATH settings",
  title = "Project Resources",
  desc = "Mandatory project resources (like pdf template) to be able to automatically generate some.",
  components = Seq()
)

wixFiles := Seq(file("target/windows/Course-Description-Automation.wxs"))

lazy val writeWixConfig = taskKey[Unit]("A task that prints result of generateComponentsAndDirectoryXml")
writeWixConfig := {
    println("-----")
    /* println(comp) */
    println("\n-----\n")
    IO.write(file("./target/windows/res-dir-xml.xml"), comp._2.toString().strip().stripMargin)
    println("\n-----\n")
    // println(resources.value)
}

lazy val getResPath = taskKey[Unit]("A task that gets the res path")

getResPath := {
    println(resourceDirectory.value)
    println("-----")
    println((Compile / resourceDirectory).value)
    println("-----")
    println(resDir_File)
    println("-----")
}

// HINT: TO GENERATE MSI INSTALLER RUN `sbt 'Windows / packageBin'` (or windows:packageBin but sbt says its deprecated)

/* ComponentFile(batPath), */
/* ComponentFile(jarPath) */
// , AddDirectoryToPath("bin"))
