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
  studyPlan: Map[String, (Int, CourseType)],
) {
    val HoursequestHours = f"$courseUrl/$id-$year"
    // TODO: implement retrieving fields from server response!
    // NB: fields are mutable for now to simplify initialization, => This will be removed latter on.


    // year in StudyPlan i.e 1->3 for Bachelor / 1->2 for Master ...
    val spYear:Int =  id.head.toInt // first letter of course code, TODO: find smth that works also for master and phd

    //var title = ???
    //var hoursNb: CourseHours = ???
    //var semester: Semester = ???
    //val language: String = ???

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

sealed trait CourseType
case object Mandatory extends CourseType
case object Optional extends CourseType
