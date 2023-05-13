package ch

import ch.net.{ReqHdl, Resp}
import com.google.gson.JsonObject
import Utils.crtYear


/**
 * Represents a Study Plan (i.e. Computer Science Bachelor)
 *
 * @param id String, immutable id of the studyPlan (i.e. the part without the year, that will never change, at least not supposed to)
 * @param year Int, year the course in this studyplan are given
 */
final case class StudyPlan(val id: String, val year: Int) {
    // val request = f"$studyPlanUrl/$year-$id"
    val urlId = f"$year-$id"
    // TODO: implement retrieving fields from server response

    val faculty: String = ???
    val section: String = ???
    val courses: Vector[Course] = ???
}

object StudyPlan {
    // def all: String = ReqHdl.studyPlan().get()

    /**
     * @return All StudyPlans as A `JsonObject`
     */
    def all: JsonObject = ReqHdl.studyPlan()().jsonObj

    /**
     * @param id String, id of studyPlan, if `year` is not given => id must be the exact
     * url-id (i.e. be of the form `studyPlanYear-studyPlanUrlId`)
     * @param year Int, year / version of this study plan (optional)
     * @return formatted Json response from server for details about given study plan
     */
    def get(id: String, year: Int = 0) = if (year == 0) ReqHdl.studyPlan(id).get() else ReqHdl.studyPlan(f"$year-$id")

    private def getYear(jsonObj: JsonObject): Int = jsonObj.get("academicalYear").getAsInt

    def getAbbreviations() = {
        val allCrtYear = Utils.getAsIter(all).filter(sp => getYear(sp.getAsJsonObject()) == crtYear)

    }

}
