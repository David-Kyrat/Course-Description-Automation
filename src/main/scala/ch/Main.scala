package ch

import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.mutable.ParArray
import scala.jdk.CollectionConverters._

// import java.io.File
// import java.io.{IOException, File}
import java.nio.file.{Files, Path}

import com.google.gson.JsonArray

import ch.Helpers._
import ch.Utils.{crtYear, pathOf}
import ch.net.exception._
import ch.net.{ReqHdl, Resp}
import ch.sealedconcept.CourseActivity
import ch.sealedconcept.CourseHours

/* import test.TestCourse._
import test.TestStudyPlan._
import test.{TestCourse, TestStudyPlan} */


object Main {
    private val abbrevFilePath: Path = pathOf("abbrev.tsv")

    // NB: lazy so value only get computed when needed

    /** Contains assocation (for each studyPlan) of the form : `Abbreviation -> (FullName, id)` */
    lazy val abbrevMap: Map[String, (String, String)] = getAbbrevMap
    lazy val usageMsg = "Usage: course_description_automation.jar \"[<course_codes>]#[<study_plan_codes>]\" ('#' is not optional)"

    def writeCoursDescToRes(id: String) = Utils.write(pathOf(f"$id-desc.json"), Resp.prettify(Course.get(id)))
    def writeSpDescToRes(id: String) = Utils.write(pathOf(f"sp-$id.json"), Resp.prettify(StudyPlan.get(id)))

    /**
     * Parses the java GUI input which looks something like `[code_1],...,[code_n]#[sp_1],...,[sp_m]`
     * where `code_i` is the i-th course code and `sp_j` the j-th study plan abbreviation.
     * see `/res/abbrev.tsv` for a list of abbreviations and what they correspond to.
     *
     * @param args entrypoint input (stdin) of this program. i.e. what was passed on command line (should normally be a single string)
     * @ return Pair of vectors `(Courses, StudyPlans)`
     */
    private def parseGuiInput(args: Array[String]): (ParArray[String], ParArray[String]) = {
        if (args.length <= 0) {
            val argsStr = args.mkString("\t")
            throw new IllegalArgumentException(f"Argument:\n\"$argsStr\" wrong input.\n $usageMsg")
        }
        val gui_input: String = args(0);
        val tmp = gui_input.split("#")
        if (tmp.length < 2) {
            val argsStr = args.toList.mkString("\t")
            throw new IllegalArgumentException(f"Argument:\n\"$argsStr\" wrong input.\n $usageMsg")
        }
        val courses = if (!tmp(0).isBlank) tmp(0).split(",").par.map(_.strip) else ParArray.empty[String]
        val studyPlans = if (!tmp(1).isBlank) tmp(1).split(",").par.map(_.strip) else ParArray.empty[String]
        (courses, studyPlans)
    }

    /**
     * Reads file at `res/abbrev.tsv` i.e. list of assocations ("study plan", ("abbreviation", "studyplan id"))
     * and parses it into a map.
     * @return (`studyPlan_abbreviation -> (studyPlan_id, studyPlan_fullName)`) mapping
     */
    private def getAbbrevMap: Map[String, (String, String)] = Utils
        .readLines(abbrevFilePath)
        .map(_.split("\t")) // tsv file
        .map(s => (s(1), (s(0), s(2)))) // WARN: In file key is 2nd and value is 1st
        .toSet
        .toMap;

    /** 'temporary' Main that parses user input and call 'real' main see `_main(Vector[String], Vector[String])` */
    def __main(args: Array[String]) = {
        val courseSpPair = parseGuiInput(args)
        _main(courseSpPair._1, courseSpPair._2)
    }

    /** 'real' Main with parsed user input */
    def _main(courseCodes: ParArray[String], sps: ParArray[String]) = {
        if (!courseCodes.isEmpty) courseCodes.foreach(Course(_).saveToMarkdown)
        if (!sps.isEmpty) sps.foreach(StudyPlan(_).saveToMarkdown)
    }

    def main(args: Array[String]): Unit = {
        // println("\n\n")
        try {
            // println(CourseHours.ALL_MAP("Cours-séminaire"))
            __main(args)
            /* writeCoursDescToRes("D200025")
            writeCoursDescToRes("32H1464")
            writeCoursDescToRes("32B0132")
            writeCoursDescToRes("32H1461") */
        } catch {
            case re: ResourceNotFoundException => {
                Utils.log(re)
                System.err.println(re.getMessage)
                System.exit(1)
            }
            case err: Throwable => {
                Utils.log(err)
                System.err.println("An unexpected Error happened. Please try again.")
                // err.printStackTrace()
                // println("-------------\n"+ err.getMessage)
                System.exit(1)
            }
        }
        // println("\n\n")
    }
}
// testAbbrevMap()
// testStudyPlanFactory()
// testSaveStudyPlanToMarkdown()
// TestCourse.testCourseOptional()
// writeCoursDescToRes("14M258")
// testMultipleCourseToMarkdown()
// testMultipleStudyPlanToMarkdown()
