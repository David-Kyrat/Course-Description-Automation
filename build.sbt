import java.nio

// import com.typesafe.sbt.SbtNativePackager.Universal.
import com.typesafe.sbt.packager.Keys.{wixConfig, wixFeatures, wixFile, wixFiles, wixProductConfig, wixProductId, wixProductLicense, wixProductUpgradeId}
import com.typesafe.sbt.packager.windows.WixHelper.generateComponentsAndDirectoryXml
import com.typesafe.sbt.packager.windows.WixHelper.{makeIdFromFile, makeWixConfig, makeWixProductConfig}
import com.typesafe.sbt.packager.windows._
import com.typesafe.sbt.packager.windows.{WindowsDeployPlugin, WindowsFeature, WindowsKeys, WindowsPlugin, WindowsProductInfo}

// Informations relative to the packaging of this project
import Artifacts.Package
import Artifacts.Wix
// Dependency and other information about build this project
import Artifacts._
import Path.relativeTo
import sbt.IO
import sbt.file

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := vers
ThisBuild / organization := "ch"

logLevel := Level.Error
maxErrors := 2
triggeredMessage := Watched.clearWhenTriggered
//WixHelper.makeWixProductConfig()
enablePlugins(JavaAppPackaging, WindowsPlugin)

// wixProductLicense := Some(Wix.wixProductLicense)

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
  wixPackageInfo := Artifacts.Wix.wixPackageInfo
)

// NB: ---------------------------------------

lazy val cl = taskKey[Unit]("A task that gets the res path")
cl := { println("\033c") }

// --------------------------------------

/**
 * Included doc from: "https://www.scala-sbt.org/sbt-native-packager/formats/windows.html"
 * A list of file->location pairs (`Seq[(sbt.File, String)]`). This list is used to move files into a location
 * where WIX can pick up the files and generate a cab or embedded cab for the msi.
 * The WIX xml should use the relative locations in this mappings when referencing files for the package.
 *
 * @return Sequence of resources to add when packaging
 */
// Windows / mappings := Seq[(File, String)]()
// Windows / mappings := (Universal / mappings).value // default mappings

Windows / mappings := {
    // val binJar = (Compile / packageBin).value // NT: this the jar of the actual compiled source code
    // val resJar = (Compile / resourceDirectory).value
    val resJar = resDir_abs
    val launcher = launcher_file
    // Seq(resJar -> Package.jarPath)
    // Package.getJarMapping((Compile / packageBin).value, (Compile / resourceDirectory).value)
    Package.getJarMapping(resJar, launcher)
}

/* Windows/mappings ++= {
    val binJar_with_deps = File("target/wind")

} */
// Ok si on arrive a faire un directory res/md/ vide => works
wixFeatures := Seq(
  WindowsFeature(
    id = "BinaryAndPath",
    title = "Project Resources",
    desc = "Mandatory project resources (like pdf template) to be able to automatically generate some.",
    // components = Seq(ComponentFile("record.md"))
    components = Seq(ComponentFile("./Course-Descritpion-Automation.exe"))
  )
)
val x = wixFeatures

/* id: String,
  title: String,
  desc: String,
  absent: String = "allow",
  level: String = "1",
  display: String = "collapse",
  components: Seq[FeatureComponent] = Seq.empty */
/* val x =
    WindowsFeature("AddBinToPath", "Update Environment Variables", "Update PATH environment variables (requires restart).", "allow", "1", "collapse", List(AddDirectoryToPath("bin"))) */
//wixFiles := Seq(file("target/windows/Course-Description-Automation.wxs"))

lazy val comp = generateComponentsAndDirectoryXml(resDir_abs, "res")
lazy val writeWixConfig = taskKey[Unit]("A task that prints result of generateComponentsAndDirectoryXml")
writeWixConfig := {
    println("-----")
    println(comp)
    cl.value
    println(f"Node:  ${comp._2}")
    comp._1.foreach(println(_))
    println("\n")
    println("\n-----\n")
    // IO.write(file("./target/windows/res-dir-xml.xml"), comp._2.toString().strip().stripMargin)
    println("\n-----\n")
}

import complete.DefaultParsers._
import sbt.File

lazy val getResPath = taskKey[Unit]("A task that gets the res path")
lazy val getWixConfig = taskKey[Unit]("A task that prints wix related settings")
lazy val setDirectory = taskKey[Unit]("A task that write config manually to .wxs file")
lazy val relBin = taskKey[Unit]("A tasks that reload this config & packageBin & calls task setDirectory")
/* lazy val genCompXml = inputKey[(Seq[String], scala.xml.Node)]("A task generate xml for the given `File`")

genCompXml := {
    val arg: String = spaceDelimited("<arg>").parsed(0)
    val components:  (Seq[String], scala.xml.Node) = generateComponentsAndDirectoryXml(File(arg), "res")
    println(f"Node:  ${components._2}")
    components._1.foreach(println(_))
    println("\n")
    components
} */

lazy val gcx = taskKey[Unit]("")

//FIX:

/* gcx := {
    val path = Path.of("res/md")
    val x = generateComponentsAndDirectoryXml(java.nio.file.Path.toFile(), "md")
    println("\n-----\n")
    println(x)
    println("\n-----\n")
} */

setDirectory := {
    Package.setDirectory()
}

relBin := {
    cl.value
    (root / Windows / packageBin).value
    setDirectory.value
}

getWixConfig := {
    println("-----\n")
    println((Windows / mappings).value.mkString("\n"))
    println("\n-----")
    println((wixFeatures).value.mkString("\n"))
    println("\n-----")
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
