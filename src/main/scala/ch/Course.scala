package ch

import ch.ReqHdl
import ch.ReqHdl.courseUrl
import ch._
import com.google.gson.JsonArray
import scala.jdk.CollectionConverters._

import ch.CourseHours.CourseHoursBuilder

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
  section: String,
  evalMode: String,
  hoursNb: CourseHours,
  documentation: String,
  teachers: Vector[String],
  format: String,
  studyPlan: Map[String, (Int, CourseType)]
) {
    val HoursequestHours = f"$courseUrl/$id-$year"

    /**
     * We have to define an auxiliary constructor taking only 1 tuple
     * argument to be able to do some json parsing before calling the constructor
     * (in scala the first line of an auxiliary must always be a call to another constructor,)
     *
     * @param args
     * @return
     */
    /* def this(args: (String, Int, String, Semester, String, String, String, String, String, String, CourseHours, String, Vector[String], String, Map[String, (Int, CourseType)])) = {

    } */

    // TODO implement retrieving fields from server response!
    // NB fields are mutable for now to simplify initialization, => This will be removed latter on.

    // year in StudyPlan i.e 1->3 for Bachelor / 1->2 for Master ...
    val spYear: Int = id.head.toInt // first letter of course code, TODO find smth that works also for master and phd

    // var title = ???
    // var hoursNb: CourseHours = ???
    // var semester: Semester = ???
    // val language: String = ???

    // StudyPlan, Credits, IsMandatory Map (only string of studyplan otherwise we'll be stuck at construction since course Needs to have initialized Study plans and studyPlan needs initialized courses)
    // val StudyPlans: Map[String, Int, Boolean] = ???
    /* var objective: String = ???
    var description: String = ???
    var faculty: String = ???
    var section: String = ???
    var documentation: String = ???
    var evalMode: String = ???
    var teachers: Vector[String] = ??? */

    /*
     Option bc we dont know if its actually in the db (=> need to actually actively search for it)
     plus it must be immutable but need not being given at runtime
     */
    /* var format: Option[_] = ???
    var preRequisites: Option[Vector[String]] = ???
    var usefulFor: Option[Vector[String]] = ??? */
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

    private def resolveSemester(sem: String): Semester = ???

    def resolveCourseHours(jsObj: JsonObject): CourseHours = {
        val activities: JsonArray = jsObj.getAsJsonArray("activities")
        val hoursNbJsonKey = "duration" // json object with that key should hold the nb of weekly hours by activity
        val chBld = new CourseHoursBuilder()

        for (_activity <- activities.asList.asScala) {
            val activity = _activity.getAsJsonObject
            val parsedAct = CourseActivity.ALL_MAP(activity.get("type").getAsString)
            parsedAct match {
                case Cours     => chBld.lectures = activity.get(hoursNbJsonKey).getAsString.dropRight(1).toInt
                case Exercices => chBld.exercices = activity.get(hoursNbJsonKey).getAsString.dropRight(1).toInt
                case Practice  => chBld.practice = activity.get(hoursNbJsonKey).getAsString.dropRight(1).toInt // removing the 'h' for hours at the end
            }
        }
        chBld.build()
    }

    private def resolveStudyPlan(studyPlans: IndexedSeq[String]): Map[String, (Int, CourseType)] = ???

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
        val title = lectures.get("title").getAsString
        val language = lectures.get("language").getAsString
        val lectureDuration = lectures.get("duration").getAsInt()
        val semester = lectures.get("periodicity").getAsString()
        val objective = lectures.get("objective").getAsString()
        val studyPlanNames = lectures.get("intended").getAsString()
        val various = lectures.get("variousInfo").getAsString()
        val comment = lectures.get("comment").getAsString()
        val coursType = lectures.get("type").getAsString()

        // new Course(id, year, title, resolveSemester(semester), objective, description,
        //     language, , , , resolveCourseHours(), documentation, , , resolveStudyPlan()
        null
    }

    override def apply(id: String, year: Int): Course = factory(id, year)
}

sealed trait CourseType
case object Mandatory extends CourseType
case object Optional extends CourseType
