import java.nio

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.{wixConfig, wixFeatures, wixFile, wixFiles, wixProductConfig, wixProductId, wixProductLicense, wixProductUpgradeId}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.windows.WindowsFeature
import com.typesafe.sbt.packager.windows.WixHelper.generateComponentsAndDirectoryXml
import com.typesafe.sbt.packager.windows.WixHelper.{makeIdFromFile, makeWixConfig, makeWixProductConfig}
import com.typesafe.sbt.packager.windows._

// Informations relative to the packaging of this project
import Artifacts.Package
import Artifacts.Wix
// Dependency and other information about build this project
import Artifacts._
import Path.relativeTo
import sbt.IO

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "1.0"
ThisBuild / organization := "ch"

logLevel := Level.Error
maxErrors := 2
triggeredMessage := Watched.clearWhenTriggered

enablePlugins(UniversalPlugin, JavaAppPackaging, WindowsPlugin)

// NB: -------- ROOT PROJECT DEFINITION -----------

lazy val root = (project in file(".")).settings(
  name := pName,
  libraryDependencies ++= externalDeps,
  Compile / resourceDirectory := resDir_abs,
  Runtime / resourceDirectory := resDir_abs,
  // assembly
  assembly / mainClass := Some(Package.mainClass),
  assembly / assemblyJarName := Package.jarName,
  // pack info
  maintainer := Package.maintainer,
  packageSummary := Package.summary,
  packageDescription := Package.description,

  // wix build information
  wixProductId := Wix.wixProductId,
  wixProductUpgradeId := Wix.wixProductUpgradeId,
  wixProductLicense := Option(Wix.wixProductLicense),
  //
  wixFeatures += WindowsFeature(
    id = "BinaryAndPath",
    // title = "My Project's Binaries and updated PATH settings",
    title = "Project Resources",
    desc = "Mandatory project resources (like pdf template) to be able to automatically generate some.",
    components = Seq()
  )
)

// NB: ---------------------------------------

lazy val cl = taskKey[Unit]("A task that gets the res path")
cl := { println("\033c") }

// ---------------------------------------

Windows / mappings := (Universal / mappings).value
//val resDirectory = resourceDirectory.value //file("/res")

Windows / mappings ++= {
    val jar = (Compile / packageBin).value
    /* val dir = (Windows / sourceDirectory).value */
    Seq(jar -> Package.jarPath) // , (Compile / resourceDirectory).value ->  // , (dir / batName) -> batPath)
}

lazy val comp = generateComponentsAndDirectoryXml(resDir_File, "res")

// wixFiles := Seq(file("target/windows/Course-Description-Automation.wxs"))

lazy val writeWixConfig = taskKey[Unit]("A task that prints result of generateComponentsAndDirectoryXml")
writeWixConfig := { println("-----")
    /* println(comp) */
    println("\n-----\n")
    IO.write(file("./target/windows/res-dir-xml.xml"), comp._2.toString().strip().stripMargin)
    println("\n-----\n")
    // println(resources.value)
}

lazy val getResPath = taskKey[Unit]("A task that gets the res path")
lazy val getWixConfig = taskKey[Unit]("A task that prints wix related settings")

getWixConfig := {
    // println((Windows / mappings.map(s => s.toString() + "\n")))
    println((Windows / mappings).value)
}

getResPath := {
    println(root / assembly / mainClass)
    // println((root / assembly / mainClass).value.get)
    // println(resourceDirectory.value)
    println((Runtime / resourceDirectory).value)
    println("-----")
    println((Compile / resourceDirectory).value)
    println("-----")
    println(resDir_File)
    println("-----")
    println((root / Compile / resourceDirectory).value)
    println("-----")
}

// HINT: TO GENERATE MSI INSTALLER RUN `sbt 'Windows / packageBin'` (or windows:packageBin but sbt says its deprecated)

/* ComponentFile(batPath), */
/* ComponentFile(jarPath) */
// , AddDirectoryToPath("bin"))
