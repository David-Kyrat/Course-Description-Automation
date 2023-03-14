package ch

import ch.ReqHdl
import ch.ReqHdl.courseUrl
import ch._
import com.google.gson.JsonArray
import scala.jdk.CollectionConverters._

import ch.sealedconcept.CourseHours.CourseHoursBuilder
import ch.sealedconcept.{CourseType, CourseHours, Semester, CourseActivity, Cours, Exercices, Practice}
import ch.sealedconcept.SealedConceptObject
import com.google.gson.JsonElement
import scala.collection.mutable.ArrayBuffer
import ch.sealedconcept.Other

/**
 * Represents a course for a given year.
 *
 * @param id String, immutable id of this Course (i.e. the part without the year, that will never change. At least not supposed to)
 * @param year String, year this course is given
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
  teachers: Vector[String],
  studyPlan: Map[String, (Int, Option[CourseType])] //Option because i havent found the data relevant to CourseType in the DB yet
) {
    val requestUrl = f"$courseUrl/$id-$year"

    // year in StudyPlan i.e 1->3 for Bachelor / 1->2 for Master ...
    val spYear: Int = id.head.toInt // first letter of course code, TODO: find smth that works also for master and phd

    /*
     Option bc we dont know if its actually in the db (=> need to actually actively search for it)
     plus it must be immutable but need not being given at runtime
     */
    val format: Option[String] = None
    val preRequisites: Option[Vector[String]] = None
    val usefulFor: Option[Vector[String]] = None
}

object Course extends Function2[String, Int, Course] {
    import com.google.gson.JsonObject

    /**
     * @param id String, i.e. course code, if `year` is not given => id must be the exact
     * urlId (i.e. be of the form `year-code`, e.g. `2022-11X001`)
     * @param year Int, year this course was given (optional)
     * @return `JsonObject` that can be traversed like a Map with a `get()` method
     */
    def get(id: String, year: Int = 0): JsonObject = {
        val reqUrl = if (year == 0) id else f"$year-$id"
        val request: ReqHdl = ReqHdl.course(reqUrl)
        request().jsonObj
    }

    /**
     * Shortcut method for very simple data to extract (i.e. the jsonKey isn't something nested like activities.<somehting>)
     * @param jsObj JsonObject, response to get request wrapped in JsonObject
     * @param sco SealedConceptObject[T], trait representing data to extract
     * @param U, case object, i.e. realisation of trait of type T (e.g. `Semester` => `Autumn`, `Semester` is a sealed Trait and `Autumn` is a case object)
     */
    private def simpleResolveSealedConceptObject[T, U >: T](jsObj: JsonObject, sco: SealedConceptObject[T], customJsonKey: String = null): U = {
        val jsonKey = if (customJsonKey == null) sco.jsonKey else customJsonKey
        return sco.ALL_MAP(jsObj.get(jsonKey).getAsString)
    }

    private def resolveCourseHours(jsObj: JsonObject): CourseHours = {
        val activities: JsonArray = jsObj.getAsJsonArray(CourseHours.jsonKey)
        val hoursNbJsonKey = CourseHours.jsonKey2 // json object with that key should hold the nb of weekly hours by activity
        val chBld = new CourseHoursBuilder()
        val extractor = (activity: JsonObject) => activity.get(hoursNbJsonKey).getAsString.dropRight(1).toInt // removing the 'h' for hours at the end

        for (_activity <- activities.asScala) {
            val activity = _activity.getAsJsonObject
            val parsedAct = simpleResolveSealedConceptObject(activity, CourseActivity, CourseActivity.jsonKey2)

            parsedAct match {
                case Cours     => chBld.lectures = extractor(activity)
                case Exercices => chBld.exercices = extractor(activity)
                case Practice  => chBld.practice = extractor(activity)
            }
        }
        chBld.build()
    }

    /**
     * @param activitiesObj JsonObject `jsObj.get("activities").getAsJsonArray`
     * @return Vector of teachers names
     */
    private def resolveTeacherNames(activitiesObj: JsonObject): Vector[String] = {
        val activityTeachers: IndexedSeq[JsonElement] = activitiesObj.get("activityTeachers").getAsJsonArray.asScala.toIndexedSeq
        def extractor(key: String, obj: JsonObject = activitiesObj): String = obj.get(key).getAsString
        val stringBufr = new ArrayBuffer[String]()
        for (teacher <- activityTeachers) {
            var fn: String = extractor("displayFirstName", teacher.asInstanceOf[JsonObject])
            val ln = extractor("displayLastName", teacher.asInstanceOf[JsonObject])
            stringBufr += (f"$fn $ln")
        }
        stringBufr.to(Vector)
    }



    private def resolveStudyPlan(jsObj: JsonObject): Map[String, (Int, Option[CourseType])] = {
        val tmp = jsObj.get("listStudyPlan").getAsJsonArray.asScala//.toIndexedSeq
        def extractor(key: String, obj: JsonObject) = obj.get(key).getAsString
        val studyPlans: IndexedSeq[JsonObject] = tmp.map(_.asInstanceOf[JsonObject]).toIndexedSeq//.asInstanceOf[IndexedSeq[JsonObject]]
        // Goal is to create from each json object a triple containing 1.studyPlan-name, 2.credit for this coruse in that plan and whether the course is mandatory or optional
        //val y: Map[String, (Int, Option[CourseType])]  = studyPlans.map(obj => (extractor("studyPlanLabel", obj), (obj.get("planCredits").getAsInt, None))).toMap
        studyPlans.map(obj => (extractor("studyPlanLabel", obj), (obj.get("planCredits").getAsInt, None))).toMap
    }



    private def factory(id: String, year: Int): Course = {
        val jsObj = get(id, year)
        val _year = jsObj.get("academicalYear")
        val _id = jsObj.get("code")
        // Just some testing function, remove after
        assert(_id.getAsString == id)
        assert(_year.getAsInt == year)
        val v2 = "activities"
        val activities: JsonArray = jsObj.getAsJsonArray(v2)
        val lectures: JsonObject = activities.get(0).getAsJsonObject()

        def extractor(key: String, jsObj: JsonObject = lectures) = jsObj.get(key).getAsString

        val title = extractor("title")
        val language = extractor("language")
        val semester: Semester = simpleResolveSealedConceptObject(lectures, Semester, Semester.jsonKey2)
        val description = extractor("description")
        val objective = extractor("objective")
        val faculty = extractor("facultyLabel", jsObj)
        // val section = ???

        val evalMode = extractor("evaluation")
        val hoursNb = resolveCourseHours(jsObj)
        val studyPlanNames = extractor("intended")
        val documentation = extractor("bibliography")
        val various = "" // extractor("variousInformation")
        val comment = extractor("comment")
        val coursType = extractor("type")

        val teachers: Vector[String] = resolveTeacherNames(lectures)
        val studPlan: Map[String, (Int, Option[CourseType])] = resolveStudyPlan(jsObj) // TODO: PARSE STUDY PLAN

        new Course(id, year, title, semester, objective, description, language, faculty, evalMode, hoursNb, documentation, teachers, studPlan)
    }

    override def apply(id: String, year: Int): Course = factory(id, year)
}
