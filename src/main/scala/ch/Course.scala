package ch

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

import com.google.gson.JsonElement

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
final case class Course(
  id: String,
  year: Int,
  title: String,
  semester: Semester,
  objective: String,
  description: String,
  language: String,
  faculty: String,
//  section: String,
  evalMode: String,
  hoursNb: CourseHours,
  documentation: String,
  authors: Vector[String],
  studyPlan: Map[String, (Int, String)], // Option because i havent found the data relevant to CourseType in the DB yet
  various: String,
  comments: String
) {
    // val requestUrl = f"$courseUrl/$id-$year"

    /*
     Option bc we dont know if its actually in the db (=> need to actually actively search for it)
     plus it must be immutable but need not being given at runtime
     */
    val format: String = hoursNb.getFormat
    val preRequisites: Option[Vector[String]] = None
    val usefulFor: Option[Vector[String]] = None

    /**
     * Serialize `this` into a markdown file that can be used to fill
     * the html course-description template.
     *
     * The syntax of those specific markdown file is :
     *  - a yaml header
     *  - empty "body"
     */
    def saveToMarkdown() = Serializer.courseToMarkdown(this)

    def toShortString() = f"$id-$title" //String.format("%s - %s }", id, title)
}

object Course extends ((String, Int) => Course) {
    import com.google.gson.JsonObject

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
        // TODO: THROW ERROR ON != 200 RESPONSE
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
        // val activities = jsObj.getAsJsonArray(CourseHours.jsonKey).asScala.map(_.asInstanceOf[JsonObject]).toIndexedSeq
        val chBld = new CourseHoursBuilder()
        // def extractor(activity: JsonObject) = activity.get(CourseHours.jsonKey2).getAsString.dropRight(1).toInt // removing the 'h' for hours at the end
        def extractor(activity: JsonObject) = activity.getAsStr(CourseHours.jsonKey2).dropRight(1).toInt // removing the 'h' for hours at the end

        for (activity <- activities) {
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

    private def resolveStudyPlan(jsObj: JsonObject): Map[String, (Int, String)] = {
        val studyPlans: Iterable[JsonObject] = jsObj.getAsScalaJsObjIter("listStudyPlan")

        // Goal is to create from each json object a triple containing 1.studyPlan-name, 2.credit for this coruse in that plan and whether the course is mandatory or optional
        studyPlans
            .map(obj => {
                // val studyPlanLabel: String = extractor("studyPlanLabel", obj)
                val studyPlanLabel: String = obj.getAsStr("studyPlanLabel")
                val isOptional = studyPlanLabel.contains("à option")
                // if false => does not mean the course is mandatory, we just dont know (lack the info in the database)
                (studyPlanLabel, (obj.getAsInt("planCredits"), if (isOptional) "Optionnel" else "N/A"))
            })
            .toMap
    }

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
        // val activities: IndexedSeq[JsonObject] = jsObj.getAsJsonArray(CourseHours.jsonKey).asScala.map(_.asInstanceOf[JsonObject]).toIndexedSeq
        val activities: IndexedSeq[JsonObject] = jsObj.getAsScalaJsObjIter(CourseHours.jsonKey).toIndexedSeq
        val lectures: JsonObject = activities.head

        def tryExtract(key: String, default: String = "", jsObj: JsonObject = lectures) =
            tryOrElse(() => jsObj.get(key).getAsString, default)

        val title = tryExtract("title", "")
        val language = tryExtract("language", "")

        /* val spType: SPType = tryOrElse(() => resolveSpType(jsObj), () => SPType.Other)
        val spYear: String = tryOrElse(() => resolveSpYear(jsObj, id), () => "N/A") */

        val semester: Semester = simpleResolveSealedConceptObject(lectures, Semester, Semester.jsonKey2)

        val description = tryExtract("description", "")
        val objective = tryExtract("objective", "")
        val faculty = tryExtract("facultyLabel", "", jsObj)
        // val section = ???

        val evalMode = tryExtract("evaluation", "")
        val hoursNb: CourseHours = tryOrElse(() => resolveCourseHours(activities), () => CourseHours(0, 0, 0)) // default value is 0 everywhere
        val studyPlanNames = tryExtract("intended", "")
        val documentation = tryExtract("bibliography", "")
        val various = tryExtract("variousInformation", "")
        val comments = tryExtract("comment", "")
        val coursType = tryExtract("type", "")

        val teachers: Vector[String] = tryOrElse(() => resolveTeacherNames(lectures), () => Vector.empty)
        lazy val noSp = Map("Pas de cursus" -> (0, "-"))
        var studPlan: Map[String, (Int, String)] = tryOrElse(
          () => {
              val sp = resolveStudyPlan(jsObj)
              if (sp.isEmpty) noSp else sp
          },
          () => noSp
        )
        new Course(id, year, title, semester, objective, description, language, faculty, evalMode, hoursNb, documentation, teachers, studPlan, various, comments)
    }

    @throws(classOf[CourseNotFoundException])
    override def apply(id: String, year: Int = Utils.crtYear): Course = factory(id, year)
}
