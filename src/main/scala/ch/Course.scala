package ch

import ch.ReqHdl
import ch.ReqHdl.courseUrl

/**
  * Represents a course for a given year.
  *
  * @param id String, immutable id of this Course (i.e. the part without the year, that will never change. At least not supposed to)
  * @param year String, year this course is given
  */
final case class Course(id: String, year:String) {
  val request = f"$courseUrl/$id-$year"

  // year in StudyPlan i.e 1->3 for Bachelor / 1->2 for Master ...
  val spYear = ??? //TODO: implement retrieving field from server response 
  val title = ??? //TODO: idem
  val hoursNb: ClassHours = ??? //TODO: idem
  val semester: Semester = ??? //TODO: idem
  val StudyPlans: Map[StudyPlan, Int, Boolean] = ??? // StudyPlan, Credits, IsMandatory Map
  val objectives: String = ??? //TODO: idem
  val content: String = ???
  val documentation: String = ???
  val evalMode: EvalMode = ???
  val teachers: Vector[String] = ???
  val format: Optional[_] = ??? //TODO: implement format sealed trait 
  val preRequisites: Optional[Vector[String]] = ??? 
  val usefulFor: Optional[Vector[String]] = ??? // NOTE: Optional bc we dont know if its actually in the db (=> need to actually actively search for it)
  // plus it must be immutable but need not being given at runtime
}
