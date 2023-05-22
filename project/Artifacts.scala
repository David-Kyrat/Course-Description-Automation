import sbt._

object Artifacts {
    // lazy val munit = "org.scalameta" %% "munit" % "0.7.29"
    lazy val scalaBaseDep = "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
    // lazy val prettyPrintJsonLib = "io.spray" %% "spray-json" % "1.3.6"
    lazy val jsonLib = "com.google.code.gson" % "gson" % "2.10.1"
    lazy val parallelCollections = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"

    /** Sequence of all currently used Dependencies */
    lazy val externalDeps = Seq(jsonLib, parallelCollections)

    // NB:  ----------------------------- PATHS --------------------------------

    val resDir_String = "res"
    val resDir_File = file(resDir_String) // File object that can be passed to functions that doesnt accept macros like `resourceDirectory`

    val pName = "Course-Description-Automation" // project Name
    val pNameLower = "course-description-automation"

    // val batName = pNameLower + ".bat"
    // val batPath = "bin/" + batName
    object Package {
        val jarName = pNameLower + ".jar"
        val jarPath = "lib/" + jarName
        val maintainer = "Noah Munz <munz.no@gmail.com>"
        val summary = "Course-Description-Automation Installer"
        val description = """MSI Installer for the application Course-Description-Automation"""
        // wix build information
        val wixProductId = "ce07be71-510d-414a-92d4-dff47631848a"
        val wixProductUpgradeId = "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"
    }
}
