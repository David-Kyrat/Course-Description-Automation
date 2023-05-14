package ch

import scala.collection.immutable.HashMap
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.immutable.ParVector
import scala.collection.{View, mutable}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters._

import java.nio.file.Path
import java.util.stream.Collectors

import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject}

import ch.Helpers.{JsonArrayOps, JsonElementOps, JsonObjOps}
import ch.Utils.{crtYear, r}
import ch.net.ReqHdl.{baseUrl, studyPlanNodeUrl}
import ch.net.exception.StudyPlanNotFoundException
import ch.net.{ReqHdl, Resp}

/**
 * Represents a Study Plan (i.e. Computer Science Bachelor)
 *
 * @param id String, id of the studyPlan
 */
final case class StudyPlan private (id: String, courses: ParVector[Course]) {

    // TODO: implement retrieving fields from server response

}

object StudyPlan extends (String => StudyPlan) {
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
     * @param id String, id of studyPlan
     * @return formatted Json response from server for details about given study plan
     *
     * If StudyPlanNotFound :
     * @throws StudyPlanNotFoundException
     */
    @throws(classOf[StudyPlanNotFoundException])
    def get(id: String): JsonObject = {
        val reqUrl = id
        val request: ReqHdl = ReqHdl.studyPlan(reqUrl)
        val resp: Resp = request()
        if (resp.isError) throw new StudyPlanNotFoundException(reqUrl)
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
        var counts = mutable.Map[String, Int]()

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

    /**
     * Must be call on a non-empty `listTeachings` Json Array obtained while parsing `StudyPlan.get(id)`
     * i.e. first line of the constructor
     *
     * @param jsArr JsonArray of json course object
     *
     * @return LazyList of course id's. The list is lazy because java parallel stream
     * operations were applied on the given `JsonArray` and since java streams are lazily evaluated
     * the conversion to scala collection was performed using a `LazyList` to optimize performance.
     */
    private def extractCourses(jsArr: JsonArray): LazyList[String] = {
        jsArr.asList.parallelStream // not guaranteed to return a parallel stream
            .parallel // will do nothing if stream is already parellel
            .map(_.getAsStr("teachingCode"))
            .toScala(LazyList)
    }

    /**
     * Extract the `listTeaching` json array nested in children of children ... of given `jsObj`.
     * Recursively search (DFS) through object of the form `{ "children": [...], "listTeaching": [] }` nested in `jsObj`
     * until we arrived at leaf from which we can extract the non-empty array.
     * (a leaf is caracterized by a empty `children` field and a non-empty `listTeaching` `JsArray`)
     * i.e. `{ "children": [], "listTeaching": [...] }`
     *
     * @param obj the 'root' to extract all the nested arrays from
     *
     * @return Collection of
     */
    private def extracListTeachings(obj: JsonObject): mutable.ListBuffer[String] = {

        /**
         * @param jsObj current 'subtree' to explore
         * @param acc Accumulator, stores the `"listTeaching" : [ ... ]`
         */
        def extractLtRec(jsObj: JsonObject, acc: mutable.ListBuffer[JsonArray]) = { 

        }
        null
    }

    /**
     * Factory methods that builds an Instance of `StudyPlan` by fetching data
     * from the http request and parses / resolve its result
     *
     * @param id studyplan code (present in `res/abbrev.tsv`)
     *
     * If request is malformed:
     *
     * @throws StudyPlanNotFoundException
     */
    @throws(classOf[StudyPlanNotFoundException])
    override def apply(id: String): StudyPlan = {
        val obj = get(id)
        val x = mutable.ListBuffer[JsonArray]()
        val y = x.flatMap(extractCourses(_))

        null
    }
}
