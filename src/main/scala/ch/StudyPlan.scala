package ch

import ch.Helpers.JsonArrayOps
import ch.Helpers.JsonObjOps
import ch.net.ReqHdl
import ch.net.Resp
import ch.net.exception.StudyPlanNotFoundException
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

import java.nio.file.Path
import scala.collection.View
import scala.collection.immutable.HashMap
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.immutable.ParVector

import Utils.{crtYear, r}

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
    private lazy val cleaningsToApply = Vector(
      "Baccalauréat universitaire en" -> "Bachelor en",
      "Baccalauréat universitaire" -> "Bachelor",
      "Baccalauréat univ." -> "Bachelor",
      "Maîtrise universitaire en" -> "Master en",
      "Maîtrise universitaire" -> "Master",
      "Maîtrise univ. en" -> "Master",
      "Maîtrise univ." -> "Master",
      "(en cours de saisie)" -> "",
      " ès " -> " ",
      " </I>" -> ""
    )

    /**
     * @return All StudyPlans of current year (i.e. `Utils.crtYear`) as a vector of `JsonArray` (i.e. extract the array in the '_data' field for each 'response page')
     */
    lazy val ALL: Vector[JsonObject] = ReqHdl.AllStudyPlan().filter(sp => getYear(sp) == crtYear).toVector // slow avoid using it (even parallelized & optimized)

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
    /* import scala.util.{Try, Success, Failure}

    private def getYearTry(jsonObj: JsonObject): Try[Int] = Try(jsonObj.get("academicalYear").getAsInt) */

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

    private lazy val toSkip: Set[String] = Set(
      "of",
      "in",
      "for",
      "the",
      "ès",
      "&",
      "la",
      "le",
      "l'",
      "de",
      "d'",
      "du",
      "et",
      "en",
      "aux",
      "au",
      "des",
      ",",
      ";",
      ":",
      "\"",
      "'",
      "-",
      ".",
      "_",
      "'",
      "/",
      "",
      "<",
      "(",
      ")",
      " "
    )
    private lazy val postCleanDelete: Set[Char] = Set('<', '(', ')', ':', '>', ';', '/', '>')
    private lazy val postCleanReplace: Map[Char, Char] = HashMap('À' -> 'A', 'È' -> 'E', 'É' -> 'E')
    // private lazy val postCleanReplaceKeys = postCleanReplace.keySet

    // TODO: ADD INDEX TO MAKE abbrevations UNIQUE

    /**
     * Extract Pair of information to be added to a Map.
     * abbreviation is created to be the first letter of each relevent word in `cleanSpName`
     * i.e. each one that's not in `toSkip`
     *
     * @param cleanSpName Cleaned name for the study plan to get the abbreviated name from
     * @param id study plan id to allow faster access later on
     * @return Pair `(Abbreviation, ()Id, Clean_SudyPlan_Name)`
     */
    private def extractAbbrev(cleanSpName: String, id: String): (String, (String, String)) =
        (cleanAbbrev(cleanSpName.split(" ").view.filterNot(toSkip.contains).map(_.head.toUpper).mkString), (id, cleanSpName))

    /** Clean any potential residual junk from abbreviation */
    private def cleanAbbrev(abbrev: String): String = abbrev.view.filterNot(postCleanDelete.contains).map(c => postCleanReplace.getOrElse(c, c)).mkString

    // NOTE: This method will only be called to create `abbrev.tsv` later on the map will be created by just reading that file

    /**
     * If there is a conflict on abbrevations (i.e. not unique) and index will be added to differentiate them.
     * @param input to uniquify i.e. Vector of the form `(abbrev, (id, cleanName))`
     * @return uniquified vector
     */
    private def uniquifyAbbrev(input: View[(String, (String, String))]): View[(String, (String, String))] = {
        var counts = Map[String, Int]()
        input.map { case (abbrev, (id, cleanName)) =>
            val count = counts.getOrElse(abbrev, 0)
            counts += (abbrev -> (count + 1))
            if (count == 0) (abbrev, (id, cleanName))
            else (s"$abbrev$count", (id, cleanName))
        }
    }

    /**
     * The name of each Study Plan is far from being consistant so a unique abbreviation has been assigned to each to
     * suppress all kind ambiguity from user input or else. (e.g. "Bachelor en Sciences Informatiques" => "BSI").
     * Each study plan id has been added to this map to not have to refetch it all the time.
     *
     * These pair will be written to a file named `abbrev.tsv`, sorted according to `Clean_SudyPlan_Name` to allow easier searching in file.
     * if there is a conflict on abbrevations (i.e. not unique) and index will be added to differentiate them.
     *
     * @return Sorted Vector of abbrevations i.e. each element is of the form `(Abbreviation, (Id, Clean_SudyPlan_Name))`
     */
    private def getAbbreviationsUniquifiedSorted(): Vector[(String, (String, String))] = uniquifyAbbrev(getAbbreviations.seq.view.distinctBy(_._2._2)).toVector.sortBy(_._2._2)

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
        ReqHdl
            .AllStudyPlan()
            .filter(sp => getYear(sp) == crtYear)
            .map(sp => extractAbbrev(cleanSpName(Utils.tryOrElse(() => sp.getAsStr("fullFormationLabel"), () => sp.getAsStr("label"), false)), sp.getAsStr("entityId")))
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
        val content = getAbbreviationsUniquifiedSorted().view
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
