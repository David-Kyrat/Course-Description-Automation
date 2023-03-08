package ch

import ch.ReqHdl
import ch.ReqHdl.courseUrl
import ch._

/**
 * Represents a course for a given year.
 *
 * @param id String, immutable id of this Course (i.e. the part without the year, that will never change. At least not supposed to)
 * @param year String, year this course is given
 */
final case class Course(id: String, year: String) {
    val HoursequestHours= f"$courseUrl/$id-$year"
    // TODO: implement retrieving fields from server response!
    // NB: fields are mutable for now to simplify initialization, => This will be removed latter on.

    // year in StudyPlan i.e 1->3 for Bachelor / 1->2 for Master ...
    var spYear = ???


    var title = ???


    var hoursNb: CourseHours = ???


    var semester: Semester = ???



    // StudyPlan, Credits, IsMandatory Map (only string of studyplan otherwise we'll be stuck at construction since course Needs to have initialized Study plans and studyPlan needs initialized courses)
    // ral StudyPlans: Map[String, Int, Boolean] = ???
    var objectives: String = ???


    var content: String = ???


    var documentation: String = ???


    var evalMode: String = ???


    var teachers: Vector[String] = ???

    /*
     Option bc we dont know if its actually in the db (=> need to actually actively search for it)
     plus it must be immutable but need not being given at runtime
     */
    var format: Option[_] = ???
    var preRequisites: Option[Vector[String]] = ???
    var usefulFor: Option[Vector[String]] = ???
}
