package ch

// import ch.Resp.
import ch.Utils.crtYear
import ch.io.Serializer
import ch.net.{ReqHdl, Resp}
import ch.net.exception.ResourceNotFoundException
import ch.net.exception._
import test.{TestCourse, TestStudyPlan}
import test.TestCourse._

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import scala.collection.parallel.immutable.ParVector
import java.io.IOException

object Main {
    private val abbrevFilePath: Path = Path.of("res/abbrev.tsv")
    val abbrevMap: Map[String, String] = getAbbrevMap()

    def writeCoursDecsToRes(id: String, year: Int = crtYear) = Utils.write(Path.of(f"res/$id-desc.json"), ReqHdl.course(f"$year-$id").get())

    /**
     * Parses the java GUI input which looks something like `<code_1>,...,<code_n>#<sp_1>,...,<sp_m>`
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
     * Reads file at `res/abbrev.tsv` i.e. list of assocations ("study plan", "abbreviation")
     * and parses it into a map.
     * @return (`study_plan -> abbreviation`) mapping
     */
    private def getAbbrevMap(): Map[String, String] = Utils
        .readLines(abbrevFilePath)
        .map(_.split("\t"))
        .map(s => (s(1), s(0))) // WARNING: In file key is 2nd and value is 1st !
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
        val spNames: Vector[String] = sps.map(abbrevMap)

        courses.foreach(_.saveToMarkdown()) // generate markdown for all courses
    }

    def main(args: Array[String]): Unit = {
        println("\n\n")
        // __main(args)
        val x = new CourseNotFoundException("test_in_main-c")
        val y = new IOException("test")
        println(x)
        println(x.getMessage())
        println(y)

        // writeCoursDecsToRes("14M252")
        // testJsonLib()
        // testResolveCoursHours()
        // testCourseFactoryMethod()
        // testCourseToMarkdown()
        // testMultipleCourseToMarkdown()

        println("\n\n")

    }
}
