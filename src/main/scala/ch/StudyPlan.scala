package ch

import ch.net.ReqHdl
import ch.net.Resp
import ch.Helpers.JsonArrayOps
import ch.net.exception.StudyPlanNotFoundException

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import java.nio.file.Path
import scala.collection.parallel.immutable.ParVector

import Utils.{crtYear, r}
import com.google.gson.JsonArray

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
    val abbrevFilePath: Path = Utils.pathOf("abbrev.tsv")

    private lazy val cleaningsToApply = Map(
        "Baccalauréat universitaire en" -> "Bachelor en"
    )
    // def all: String = ReqHdl.studyPlan().get()

    /**
     * @return All StudyPlans as A `JsonArray`
     */
    def all: JsonArray = ReqHdl.studyPlan().apply.jsonObj.getAsJsonArray("_data")

    /**
     * @param id String, id of studyPlan, if `year` is not given => id must be the exact
     * url-id (i.e. be of the form `studyPlanYear-studyPlanUrlId`)
     * @param year Int, year / version of this study plan (optional)
     * @return formatted Json response from server for details about given study plan
     *
     * If StudyPlanNotFound: 
     *
     * @throws StudyPlanNotFoundException
     */
    @throws(classOf[StudyPlanNotFoundException])
    def get(id: String, year: Int = 0): JsonObject = {
        val reqUrl = if (year == 0) id else f"$year-$id"
        val request: ReqHdl = ReqHdl.studyPlan(reqUrl)
        // TODO: THROW ERROR ON != 200 RESPONSE
        val resp: Resp = request()
        if (resp.isError) throw new StudyPlanNotFoundException(f"$year-$id")
        else resp.jsonObj
    }

    private def getYear(jsonObj: JsonObject): Int = jsonObj.get("academicalYear").getAsInt

    /**
     * Apply cleanings defined in `cleaningsToApply`
     *
     * @param fullFormationLabel study plan name to apply cleaning on
     * @return cleaned name
     */
    private def cleanSpName(fullFormationLabel: String): String = {
        for (kv <- cleaningsToApply) {
            fullFormationLabel.replace(kv._1, kv._2)
        }
        fullFormationLabel
    }

    /**
     * Extract Pair of information to be added to a Map
     * @param cleanSpName Cleaned name for the study plan to get the abbreviated name from
     * @param id study plan id to allow faster access later on
     * @return Pair `(Abbreviation, Id, Clean_SudyPlan_Name)`
     */
    private def extractAbbrev(cleanSpName: String, id: String): (String, (String, String)) = ???

    // NOTE: This method will only be called to create `abbrev.tsv` later on the map will be created by just reading that file

    /**
     * The name of each Study Plan is far from being consistant so a unique abbreviation has been assigned to each to
     * suppress all kind ambiguity from user input or else. (e.g. "Bachelor en Sciences Informatiques" => "BSI").
     * Each study plan id has been added to this map to not have to refetch it all the time.
     *
     * Content of this Map will be written to a file named `abbrev.tsv`
     *
     * @return ParVector of abbrevations i.e. each element is of the form `(Abbreviation, (Id, Clean_SudyPlan_Name))`
     */
    private def getAbbreviations(): ParVector[(String, (String, String))] = {
        // val allCrtYear: Iterable[JsonObject] = Utils.getAsJsonObjIter(all).filter(sp => getYear(sp) == crtYear)
        val allCrtYear: Iterable[JsonObject] = all.getAsScalaJsObjIter.filter(sp => getYear(sp) == crtYear)
        allCrtYear
            .to(ParVector)
            .map(sp => 
                    extractAbbrev(
                        cleanSpName(sp.get("fullFormationLabel").getAsString),
                        sp.get("entityId").getAsString
                    )
                )
    }

    /**
     * Create content of `abbrev.tsv`.
     *
     * Reason why this file is needed:
     *
     * The name of each Study Plan is far from being consistant so a unique abbreviation has been assigned to each to
     * suppress all kind ambiguity from user input or else. (e.g. "Bachelor en Sciences Informatiques" => "BSI").
     * Each study plan id has been added as well to not have to refetch it all the time.
     */
    def createAbbrevFile() = {
        val content: String = getAbbreviations()
            .map(ppair => {
                val abbrev = ppair._1
                val pair = ppair._2
                val id = pair._1
                val cleanSpName = pair._2
                f"${cleanSpName}\t${abbrev}\t${id}" // DONT CHANGE ORDER
            })
            .mkString("\n")
        val _ = Utils.write(abbrevFilePath, content, false)
    }

}
