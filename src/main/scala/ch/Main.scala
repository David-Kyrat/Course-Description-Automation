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
            throw new IllegalArgumentException(f"Expect at least 1 argument. Argument:\n\"$argsStr\" wrong input.\n $usageMsg")
        }
        val _gui_input: String = args(0).trim();
        // if gui_input has a "#" but nothing after i.e. "12X001#" program will fail
        // since apparently the split array is only of length 1.
        // So checking that special case here.
        val gui_input = if (_gui_input.endsWith("#")) _gui_input + " " else _gui_input
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
        println(f"written courses in ${courseCodes.mkString("\n")}")
        println(f"\nwritten courses in ${sps.mkString("\n")}")

    }

    def main(args: Array[String]): Unit = {
        try {
            __main(args)
        } catch {
            case re: ResourceNotFoundException => {
                Utils.log(re)
                System.err.println(re.getMessage)
                System.exit(1)
            }
            case err: Throwable => {
                Utils.log(err)
                System.err.println("An unexpected error happened during the pdf generation. Please try again.")
                err.printStackTrace()
                // println("-------------\n"+ err.getMessage)
                // System.exit(1)
            }
        }
    }
}
// testAbbrevMap()
// testStudyPlanFactory()
// testSaveStudyPlanToMarkdown()
// TestCourse.testCourseOptional()
// writeCoursDescToRes("14M258")
// testMultipleCourseToMarkdown()
// testMultipleStudyPlanToMarkdown()
