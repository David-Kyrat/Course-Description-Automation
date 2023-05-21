package ch

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import ch.Helpers.{JsonElementOps, JsonObjOps}
import ch.Utils.tryOrElse
import ch.io.Serializer
import ch.net.ReqHdl.courseUrl
import ch.net.exception.CourseNotFoundException
import ch.net.{ReqHdl, Resp}
import ch.sealedconcept.CourseHours.CourseHoursBuilder
import ch.sealedconcept._

/**
 * Represents a course for a given year.
 *
 * @param id String, immutable id of this Course (i.e. the part without the year, that will never change. At least not supposed to)
 * @param year String, year this course is given
 *
 * If course not found:
 *
 * @throws CourseNotFoundException
 */
final case class Course private (
  id: String,
  year: Int,
  title: String,
  semester: Semester,
  objective: String,
  description: String,
  language: String,
  faculty: String,
  evalMode: String,
  hoursNb: CourseHours,
  documentation: String,
  authors: Vector[String],
  studyPlan: Map[String, (Float, String)],
  various: Option[String],
  comments: Option[String],
  prerequisites: Option[String],
  listenerAccepted: Option[Boolean],
  publicAccepted: Option[Boolean]
) {

    /*
     Option bc we dont know if its actually in the db (=> need to actually actively search for it)
     plus it must be immutable but need not being given at runtime
     */
    val format: String = hoursNb.getFormat

    /**
     * Serialize `this` into a markdown file that can be used to fill
     * the html course-description template.
     *
     * The syntax of those specific markdown file is :
     *  - a yaml header
     *  - empty "body"
     */
    def saveToMarkdown() = Serializer.courseToMarkdown(this)
    def toShortString() = f"$id-$title"

}

object Course extends ((String, Int) => Course) {

    /**
     * @param id String, i.e. course code, if `year` is not given => id must be the exact urlId (i.e. be of the form `year-code`, e.g. `2022-11X001`)
     * @param year Int, year this course was given (optional)
     * @return `JsonObject` that can be traversed like a Map with a `get()` method
     *
     * If Course Not Found (do not catch):
     * @throws CourseNotFoundException
     */
    @throws(classOf[CourseNotFoundException])
    def get(id: String, year: Int = 0): JsonObject = {
        val reqUrl = if (year == 0) id else f"$year-$id"
        val request: ReqHdl = ReqHdl.course(reqUrl)
        val resp: Resp = request()
        if (resp.isError) throw new CourseNotFoundException(f"$year-$id")
        else resp.jsonObj
    }

    /**
     * Shortcut method for very simple data to extract (i.e. the jsonKey isn't something nested like activities.<somehting>)
     * @param jsObj JsonObject, response to get request wrapped in JsonObject
     * @param sco SealedConceptObject[T], trait representing data to extract
     * @param U, case object, i.e. realisation of trait of type T (e.g. `Semester` => `Autumn`, `Semester` is a sealed Trait and `Autumn` is a case object)
     */
    private def simpleResolveSealedConceptObject[T, U >: T](jsObj: JsonObject, sco: SealedConceptObject[T], customJsonKey: String = null): U = {
        val jsonKey = if (customJsonKey == null) sco.jsonKey else customJsonKey
        // return sco.ALL_MAP(jsObj.get(jsonKey).getAsString)
        return sco.ALL_MAP(jsObj.getAsStr(jsonKey))
    }

    /**
     * Takes the sequence of jsonObject representing the jsonArray of "activities" of each course
     * (they are represented by `sealedconcept.CourseActivity`)
     * @param activities result of `obj.get("activities").getAsJsonArray`
     * (`obj` is the json response for the details of this course i.e. `Course.get(this.year, this.id)`)
     * @return resolved instance of `CourseHours`
     */
    private def resolveCourseHours(activities: IndexedSeq[JsonObject]): CourseHours = {
        val chBld = new CourseHoursBuilder()
        def extractor(activity: JsonObject) = activity.getAsStr(CourseHours.jsonKey2).dropRight(1).toFloatOption match {
            case Some(value) => value
            case None        => 0
        } // removing the 'h' for hours at the end

        for (activity <- activities) {
            if (activity.getAsStr("type").equals("Cours-séminaire")) {
                println("COURSEM")
                println(activity.getAsStr("duration"))
            }
            val ca: CourseActivity = simpleResolveSealedConceptObject(activity, CourseActivity, CourseActivity.jsonKey2)
            chBld.setActivity(ca, extractor(activity))
        }
        chBld.build()
    }

    /**
     * @param activitiesObj JsonObject `jsObj.get("activities").getAsJsonArray`
     * @return Vector of teachers names
     */
    private def resolveTeacherNames(activitiesObj: JsonObject): Vector[String] = {
        val activityTeachers: IndexedSeq[JsonObject] = activitiesObj.getAsScalaJsObjIter("activityTeachers").toIndexedSeq

        def extractor(key: String, obj: JsonObject = activitiesObj): String = obj.getAsStr(key)
        val stringBufr = new ArrayBuffer[String]()
        for (teacher <- activityTeachers) {
            var fn: String = teacher.getAsStr("displayFirstName")
            val ln = teacher.getAsStr("displayLastName")
            stringBufr += (f"$fn $ln")
        }
        stringBufr.to(Vector)
    }

    private def resolveStudyPlan(jsObj: JsonObject): Map[String, (Float, String)] = {
        val studyPlans: Iterable[JsonObject] = jsObj.getAsScalaJsObjIter("listStudyPlan")
        // try {
        // Goal is to create from each json object a triple containing 1.studyPlan-name, 2.credit for this coruse in that plan and whether the course is mandatory or optional
        studyPlans
            .map(obj => {
                val studyPlanLabel: String = obj.getAsStr("studyPlanLabel")
                val isOptional = studyPlanLabel.contains("à option")
                // if false => does not mean the course is mandatory, we just dont know (lack the info in the database)
                (studyPlanLabel, (tryOrElse(() => obj.get("planCredits").getAsFloat(), 0f, f"$studyPlanLabel", logErr = false), if (isOptional) "Optionnel" else "N/A"))
            })
            .toMap
    }

    /**
     * @param lectureActivity json object courseObj.activities[0] (activities is a jsonArray)
     */
    private def parseOptionals(lectureActivity: JsonObject): (Option[String], Option[Boolean], Option[Boolean]) = (
      tryOrElse(() => Option(lectureActivity.getAsStr("recommended")), () => None, logErr = false),
      tryOrElse(() => Option(lectureActivity.get("listenerAccepted").getAsBoolean), () => None, logErr = false),
      tryOrElse(() => Option(lectureActivity.get("publicAccepted").getAsBoolean), () => None, logErr = false),
    )

    /**
     * Factory methods that builds an Instance of `Course` by fetching data
     * from the http request and parses / resolve its result
     *
     * @param id  course code
     * @param year year this course was given (optional defaults to Utils.crtYear)
     * @return new instance of `Course`
     *
     * @throws CourseNotFoundException
     */
    private def factory(id: String, year: Int = Utils.crtYear): Course = {
        val jsObj = get(id, year)
        val v2 = "activities"
        val activities: IndexedSeq[JsonObject] = jsObj.getAsScalaJsObjIter(CourseHours.jsonKey).toIndexedSeq
        val lectures: JsonObject = activities.head
        lazy val additionalErrMsg = f"COURSE_ID: $id"

        def tryExtract(key: String, default: String = "", msg: String = "X", jsObj: JsonObject = lectures, log: Boolean = true): String =
            tryOrElse(() => jsObj.get(key).getAsString, () => default, additionalErrMsg + f"| FIELD: $msg", logErr = log)

        def tryExtractOpt(key: String, default: Option[String] = None, jsObj: JsonObject = lectures): Option[String] =
            tryOrElse(() => Some(jsObj.get(key).getAsString), () => default, logErr = false)

        val title = tryExtract("title", "", "TITLE")
        val language = tryExtract("language", "LANG")

        val semester: Semester = simpleResolveSealedConceptObject(lectures, Semester, Semester.jsonKey2)

        val description = tryExtract("description", "", "DESCRIPTION", log = false)
        val objective = tryExtract("objective", "", "OBJECTIVE", log = false)
        val faculty = tryExtract("facultyLabel", "", "faculty", jsObj)

        val evalMode = tryExtract("evaluation", "", "EVAL_MODE", log = false)
        val hoursNb: CourseHours = tryOrElse(
          () => resolveCourseHours(activities),
          () => {
              // if (activities.head.getAsStr(""))
              CourseHours(0, 0, 0)
          },
          f"COURSEHOURS $id"
        ) // default value is 0 everywhere
        val documentation = tryExtract("bibliography", "", "DOCUMENTATION", log = false)
        val various: Option[String] = tryExtractOpt("variousInformation")
        val comments: Option[String] = tryExtractOpt("comment")
        val coursType = tryExtract("type", "", "COURSE TYPE", log = false)

        val teachers: Vector[String] = tryOrElse(() => resolveTeacherNames(lectures), () => Vector.empty)
        lazy val noSp = Map("No cursus" -> (0f, "\\-"))
        var studPlan: Map[String, (Float, String)] = { // tryOrElse(
            // () => {
            val sp = resolveStudyPlan(jsObj)
            // if (sp == null || sp.isEmpty) noSp else sp
            if (sp == null || sp.isEmpty) null else sp
            // },
            // () => noSp
        }
        // )
        val triplOpt = parseOptionals(lectures)

        new Course(
          id,
          year,
          title,
          semester,
          objective,
          description,
          language,
          faculty,
          evalMode,
          hoursNb,
          documentation,
          teachers,
          studPlan,
          various,
          comments,
          triplOpt._1,
          triplOpt._2,
          triplOpt._3
        )
    }
    /* def test(id: String, throwable: Throwable = null) = {
        Utils.log(throwable, f"Course: $id")
        throwable.printStackTrace()
        System.exit(1)
        throw new IllegalStateException() // apparently compiler cant tell that theres no need for a return value if program exits
    } */

    // @throws(classOf[CourseNotFoundException])
    override def apply(id: String, year: Int = Utils.crtYear): Course = factory(id, year)
    /*{
        try {
            val x = Try(factory(id, year))
            x match {
                case Success(value)     => value
                case Failure(exception) => test(id, exception)
            }

        } catch {
            case _: Throwable => test(id)
        }
    }*/
}
