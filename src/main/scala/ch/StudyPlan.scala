package ch

import ch.net.ReqHdl
import ch.net.Resp
import ch.Helpers.{JsonArrayOps, JsonObjOps}
import ch.net.exception.StudyPlanNotFoundException

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import java.nio.file.Path
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.CollectionConverters._

import Utils.{crtYear, r}
import com.google.gson.JsonArray
import scala.collection.immutable.HashMap
import com.google.gson.JsonNull

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

    /** WARN: APPLY LINEARLY IN ORDER! */
    private lazy val cleaningsToApply = Vector("Baccalauréat universitaire en" -> "Bachelor en", 
        "Maîtrise universitaire en" -> "Master en", 
        "Maîtrise universitaire" -> "Master",
        "Maîtrise univ. en" -> "Master",
        "Maîtrise univ." -> "Master"
        )

    /**
     * @return All StudyPlans of current year as a vector of `JsonArray` (i.e. extract the array in the '_data' field for each 'response page')
     */
    lazy val all: Vector[JsonObject] = ReqHdl.AllStudyPlan().filter(sp => getYear(sp) == crtYear).toVector // slow avoid using it (even parallelized & optimized)

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
        val resp: Resp = request()
        if (resp.isError) throw new StudyPlanNotFoundException(f"$year-$id")
        else resp.jsonObj
    }

    private def getYear(jsonObj: JsonObject): Int = jsonObj.get("academicalYear").getAsInt
    import scala.util.{Try, Success, Failure}

    private def getYearTry(jsonObj: JsonObject): Try[Int] = Try(jsonObj.get("academicalYear").getAsInt)

    /**
     * Apply cleanings defined in `cleaningsToApply`
     *
     * @param fullFormationLabel study plan name to apply cleaning on
     * @return cleaned name
     */
    private def cleanSpName(fullFormationLabel: String): String = {
        var crt = fullFormationLabel
        for (kv <- cleaningsToApply) crt = crt.replace(kv._1, kv._2)
        crt
    }

    private val toSkip: Set[String] = Set("de", "d'", "du", "en", "aux", "au", "des", ",", ";", "\"", "'", "-", ".", "_", "'", "/", "")

    /**
     * Extract Pair of information to be added to a Map.
     * abbreviation is created to be the first letter of each relevent word in `cleanSpName`
     * i.e. each one that's not in `toSkip`
     *
     * @param cleanSpName Cleaned name for the study plan to get the abbreviated name from
     * @param id study plan id to allow faster access later on
     * @return Pair `(Abbreviation, Id, Clean_SudyPlan_Name)`
     */
    private def extractAbbrev(cleanSpName: String, id: String): (String, (String, String)) =
        (cleanSpName.split(" ").view.filterNot(toSkip.contains).map(_.head.toUpper).mkString, (id, cleanSpName))

    // NOTE: This method will only be called to create `abbrev.tsv` later on the map will be created by just reading that file

    /**
     * The name of each Study Plan is far from being consistant so a unique abbreviation has been assigned to each to
     * suppress all kind ambiguity from user input or else. (e.g. "Bachelor en Sciences Informatiques" => "BSI").
     * Each study plan id has been added to this map to not have to refetch it all the time.
     *
     * These pair will be written to a file named `abbrev.tsv`, sorted according to `Clean_SudyPlan_Name` to allow easier searching in file.
     *
     * @return Sorted Vector of abbrevations i.e. each element is of the form `(Abbreviation, (Id, Clean_SudyPlan_Name))`
     */
    def getAbbreviationsSorted(): Vector[(String, (String, String))] = getAbbreviations.toVector.sortBy(_._2._2)

    // WARNING: Field "fullFormationLabel" or "formationLabel" are not present everywhere in the database use "label" instead

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
        val x: ParVector[Try[Int]] = ReqHdl.AllStudyPlan().map(getYearTry(_))
        val failure = x.filter(_.isFailure)
        println(f" Length of studyplan without year: " + failure.length)
        println("-------------------------------\n")
        // System.exit(1)
        ReqHdl
            .AllStudyPlan()
            .filter(sp => getYear(sp) == crtYear)
            .map(sp => extractAbbrev(cleanSpName(sp.get("label").getAsString), sp.get("entityId").getAsString))
            // .map(sp => extractAbbrev(cleanSpName(sp.get("label").getAsString), ""))
    }
    /* .toVector
            .sortBy(_._2._2) */
    /* val allCrtYear: Iterable[JsonObject] = all.getAsScalaJsObjIter.filter(sp => getYear(sp) == crtYear)
        allCrtYear
            .to(ParVector)
            .map(sp =>
                    extractAbbrev(
                        cleanSpName(sp.get("fullFormationLabel").getAsString),
                        sp.get("entityId").getAsString
                    )
                ) */

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
        val content = getAbbreviationsSorted().view
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
