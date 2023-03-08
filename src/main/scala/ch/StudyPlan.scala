package ch

import ch.ReqHdl.studyPlanUrl
import ch.Utils.prettifyJson

/**
 * Represents a Study Plan (i.e. Computer Science Bachelor)
 *
 * @param id String, immutable id of the studyPlan (i.e. the part without the year, that will never change, at least not supposed to)
 * @param year String, year the course in this studyplan are given
 */
final case class StudyPlan(val id: String, val year: String) {
    //val request = f"$studyPlanUrl/$year-$id"
    val urlId = f"$year-$id"
    // TODO: implement retrieving fields from server response

    val faculty: String = ???
    val section: String = ???
    val courses: Vector[Course] = ???
}

object StudyPlan {
    def all: String = ReqHdl.studyPlan().get()

    /**
     * @param id String, id of studyPlan, if `year` is not given => id must be the exact
     * url-id (i.e. be of the form `studyPlanYear-studyPlanUrlId`)
     * @param year String year / version of this study plan (optional)
     * @return formatted Json response from server for details about given study plan
     */
    def get(id: String, year: String = null) =
        if (year == null) ReqHdl.studyPlan(id).get() else ReqHdl.studyPlan(f"$year-$id")

}
