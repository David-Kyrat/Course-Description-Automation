package ch

import ch.Resp.prettifyJson
import ch.ReqHdl.{studyPlanUrl, g, studyPlan}

final case class StudyPlan(val id: String, val year: String) {
  val request = f"$studyPlanUrl/$year-$id"
}

object StudyPlan {
  def all(): String = studyPlan().get

  /** @param id
    *   String, id of studyPlan, if `year` is not given => id must be the exact
    *   url-id (i.e. be of the form `studyPlanUrlId-studyPlanYear`)
    * @param year
    *   String year / version of this study plan (optional)
    * @return
    *   formatted Json response from server for details about given study plan
    */
  def details(id: String, year: String = null) =
    if (year == null) studyPlan(id).get else studyPlan(f"$id-$year").get

}
