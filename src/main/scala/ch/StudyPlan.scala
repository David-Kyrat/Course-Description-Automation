package ch

import ch.Req.studyPlanUrl

final case class StudyPlan(val id: String, val name: String, val year: Int, val faculty: String) {
  val request = f"$studyPlanUrl/$year-$id"
}

