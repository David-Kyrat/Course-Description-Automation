import java.nio
import java.nio.charset.StandardCharsets.UTF_8

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.{wixConfig, wixFeatures, wixFile, wixFiles, wixProductConfig, wixProductId, wixProductLicense, wixProductUpgradeId}
import com.typesafe.sbt.packager.windows._
import com.typesafe.sbt.packager.windows.{WindowsDeployPlugin, WindowsFeature, WindowsKeys, WindowsPlugin, WindowsProductInfo}

import sbt._

object Artifacts {
    lazy val vers = "1.0"
    // lazy val munit = "org.scalameta" %% "munit" % "0.7.29"
    lazy val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
    // lazy val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
    lazy val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"
    lazy val parallelCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"

    /** Sequence of all currently used Dependencies */
    lazy val externalDeps = Seq(jsonLib, parallelCollections)

    // NB:  ----------------------------- PATHS --------------------------------

    val resDir_String = "res"
    lazy val resDir_path: nio.file.Path = nio.file.Path.of(Artifacts.resDir_String).toAbsolutePath
    lazy val resDir_abs: java.io.File = resDir_path.toFile

    val resDir_File = file(resDir_path.toString) // File object that can be passed to functions that doesnt accept macros like `resourceDirectory`

    val pName = "Course-Description-Automation" // project Name
    val pNameLower = "course-description-automation"

    // val batName = pNameLower + ".bat"
    // val batPath = "bin/" + batName
    object Package {
        val mainClass = "ch.Main"
        val jarName = pNameLower + ".jar"
        val jarPath = "lib/" + jarName
        val maintainer = "Noah Munz <munz.no@gmail.com>"
        val summary = "Course-Description-Automation Installer"
        // val description = """MSI Installer for the application Course-Description-Automation"""
        val description = """All the mandatory files (core, libraries and resources) for the project to run."""

        /**
         * Return mapping `file->location` to be added
         * to the `Windows / mappings configuration`.
         * ----
         * ### Included doc from: "https://www.scala-sbt.org/sbt-native-packager/formats/windows.html"**
         * > A list of `file->location` pairs (`Seq[(sbt.File, String)]`). This list is used to move files into a location
         * > where WIX can pick up the files and generate a cab or embedded cab for the msi.
         * > The WIX xml should use the relative locations in this mappings when referencing files for the package.
         * ----
         * Tl;Dr: We need to give a pair "(resourceToAdd, jarToAddItTo)" and since
         * the path to the packaged jar is defined by `Package.jarPath` this function
         * takes in file and return mapping that maps this file to the `jarPath
         * @param res resources to add when packaging
         * @return Pair described above
         */
        def getJarMapping(res: java.io.File*): Seq[(java.io.File, String)] = res.map(r => (r -> Package.jarPath))

        // Write manually some config in file
        def setDirectory(): Unit = {
            val path = nio.file.Path.of("C:/Users/noahm/DocumentsNb/BA4/Course-Description-Automation/target/windows/Course-Description-Automation.wxs")
            val content: StringBuilder = new StringBuilder(nio.file.Files.readString(path, UTF_8))
            if (!content.substring(0, 1000).contains("Set")) {
                val toInsert: String = String.format("<SetDirectory Id=\"INSTALLDIR\" Value=\"[PersonalFolder]%s\" />", pName)
                var patt1 = "<Directory"
                val idx = content.indexOf(patt1) - 2 // + patt1.length
                content.insertAll(idx, toInsert.toArray)
                nio.file.Files.write(path, content.toString.getBytes(UTF_8))
            }

        }
    }

    object Wix {
        // wix build information
        val wixTitle = pName
        val wixProductId = "ce07be71-510d-414a-92d4-dff47631848a"
        val wixProductUpgradeId = "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"
        // val wixProductLicense = file("LICENSE")
        val wixInstallScope = "perUser"
        val wixCompressed = true
        lazy val wixPackageInfo =
            WindowsProductInfo(Wix.wixProductId, wixTitle, vers, Package.maintainer, Package.description, Wix.wixProductUpgradeId, "nil", wixInstallScope, "200", wixCompressed)
    }
}
/*
    lazy val wixPackageInfo = WindowsProductInfo(
        Wix.wixProductId,
        wixTitle,
        version,
        Package.maintainer,
        Package.description,
        Wix.wixProductUpgradeId,
        "",
        wixInstallScope,
        "200",
        wixCompressed
    )
 */
