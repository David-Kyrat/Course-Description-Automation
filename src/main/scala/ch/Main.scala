package ch

// import ch.Resp.
import ch.Utils.crtYear
import ch.Utils.pathOf
import ch.Utils.r
import ch.io.Serializer
import ch.net.ReqHdl
import ch.net.Resp
import ch.net.exception._
import ch.Helpers._

import test.TestCourse
import test.TestCourse._
import test.TestStudyPlan

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import scala.collection.parallel.immutable.ParVector
import com.google.gson.JsonArray
import scala.jdk.CollectionConverters._

object Main {
    private val abbrevFilePath: Path = pathOf("abbrev.tsv")

    // NB: lazy so value only get computed when needed

    /** Contains assocation (for each studyPlan) of the form : `Abbreviation -> (FullName, id)` */
    lazy val abbrevMap: Map[String, (String, String)] = getAbbrevMap()

    def writeCoursDescToRes(id: String, year: Int = crtYear) = Utils.write(pathOf(f"$id-desc.json"), ReqHdl.course(f"$year-$id")().prettify)

    /**
     * Parses the java GUI input which looks something like `[code_1],...,[code_n]#[sp_1],...,[sp_m]`
     * where `code_i` is the i-th course code and `sp_j` the j-th study plan abbreviation.
     * see `/res/abbrev.tsv` for a list of abbreviations and what they correspond to.
     *
     * @param args entrypoint input (stdin) of this program. i.e. what was passed on command line (should normally be a single string)
     * @ return Pair of vectors `(Courses, StudyPlans)`
     */
    private def parseGuiInput(args: Array[String]): (Vector[String], Vector[String]) = {
        if (args.length <= 0) { throw new IllegalArgumentException("Usage: course_description_automation.jar <gui_input>") }
        val gui_input: String = args(0);
        val tmp = gui_input.split("#")
        val courses = tmp(0).split(",").toVector
        val studyPlans = tmp(1).split(",").toVector
        (courses, studyPlans)
    }

    /**
     * Reads file at `res/abbrev.tsv` i.e. list of assocations ("study plan", ("abbreviation", "studyplan id"))
     * and parses it into a map.
     * @return (`studyPlan_abbreviation -> (studyPlan_fullName, studyPlan_id)`) mapping
     */
    private def getAbbrevMap(): Map[String, (String, String)] = Utils
        .readLines(abbrevFilePath)
        .map(_.split("\t"))
        .map(s => (s(1), (s(0), s(2)))) // WARNING: In file key is 2nd and value is 1st !
        .toSet
        .toMap;

    def __main(args: Array[String]) = {
        var tmp = parseGuiInput(args)
        val course: Vector[String] = tmp._1
        val sps: Vector[String] = tmp._2
        _main(course, sps)
    }

    def _main(courseCodes: Vector[String], sps: Vector[String]) = {
        val courses: Vector[Course] = courseCodes.map(Course(_))
        // val spIds: Vector[String] = sps.map(s => abbrevMap(s)._2)

        courses.foreach(_.saveToMarkdown()) // generate markdown for all courses
    }


    /* def getSps() = {
        val x = StudyPlan.all
        val json = Resp.gson.toJson(x.asJava)
        Utils.write(pathOf(f"sp1.json"), json)
    } */


    def main(args: Array[String]): Unit = {
        println("\n\n")

        // TODO: print error message to stderr so that rust app can extract it into an error window
        // TODO: GET BACK LOGGING FUNCTIONS FROM MASTER
        try {
            // __main(args)
            // writeCoursDecsToRes("14M252")
            // testJsonLib()
            // testResolveCoursHours()
            // testCourseFactoryMethod()
            // testCourseToMarkdown()
            // testMultipleCourseToMarkdown()
        } catch {
            case re: ResourceNotFoundException => {
                System.err.println(re.getMessage())
                // System.exit(1)
            }
            case err: Exception => {
                System.err.println("An unexpected Error happened. Please try again.")
                err.printStackTrace()
                // System.exit(1)
            }
        }

        println("\n\n")

    }
}
