package ch

import com.google.gson.{Gson, GsonBuilder}
// import spray.json._

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import java.time.LocalDate
import scala.language.postfixOps
import scala.collection.immutable
//import DefaultJsonProtocol._
import java.io.{BufferedWriter, FileWriter, PrintWriter}
import scala.jdk.CollectionConverters._
import com.google.gson.JsonElement
import java.util.Collection
import scala.collection.Factory
import ch.net.ReqHdl
import scala.collection.parallel.immutable.ParVector
import com.google.gson.JsonObject
import com.google.gson.JsonArray

final object Utils {
    private val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
    private val logPath = Path.of(r("log/err.log")).toAbsolutePath
    write(logPath, "") // prevents logfile content from getting to big by cleaning it
    private val errLogPrintWriter = new PrintWriter(new BufferedWriter(new FileWriter(logPath.toString, UTF_8, true)), true)
    private val sep = "---------------------------------------\n\n"

    // TODO:
    // FIX:  ADD REAL PATH RESOLVING SIMULATING WHERE THE COMPILED JAR WILL BE
    /**
     * Resolve given 'against' resource path
     * e.g. if we want to acces `/files/res/md/test.md`
     * just enter `md/test.md`.
     * And this function will return the corresponding relative path
     * @param path resource to locate
     * @return valid relative to resource (relative w.r.t the runnable i.e. jar or else)
     */
    def r(path: String): String = f"res/$path"
    // def r(path: String): String = f"files/res/$path"

    /**
     * Wraps `Path.of(r(path))` see `Utils.r` for more info
     *
     * @param path resource to locate
     * @return valid relative to resource (relative w.r.t the runnable i.e. jar or else)
     */
    def pathOf(path: String): Path = Path.of(r(path))

    /**
     * Shorthand for `Files.readAllLines(path, UTF_8).asScala.toIndexedSeq`
     * i.e. opens, read each line into list, closes
     * @param path path to file to read
     * @return content as `IndexedSeq` (immutable)
     */
    def readLines(path: Path): immutable.IndexedSeq[String] = Files.readAllLines(path, UTF_8).asScala.toIndexedSeq

    /**
     * Shorthand for `String.join("\n", Files.readAllLines(path, UTF_8))`
     * i.e. opens, read each line into string, closes, then convert to string
     * @param path path to file to read
     * @return content as string
     */
    def read(path: Path) = String.join("\n", Files.readAllLines(path, UTF_8))

    /**
      * Write string using `Files.writeString` method (i.e. to write multiple strings do not use this method multiple times)
      * @param path Path of file to write to
      * @param content content to write to file 
      * @param append whether to add to previous content or overwrite the file
      */
    def write(path: Path, content: String, append: Boolean = false) = {
        val opt = if (append) APPEND else TRUNCATE_EXISTING
        Files.writeString(path, content, UTF_8, CREATE, opt)
    }

    /**
     * @return Lastest version for course & study plan information
     * i.e. current year - 1
     */
    def crtYear: Int = LocalDate.now.getYear - 1

    /**
     * Shorthand for `el.getAsJsonArray.asScala.to(ParVector)`
     *
     * @param el `JsonElement` to convert to scala iterable
     * @return converted collection
     */
    // def getAsParVec(el: JsonElement) = el.getAsJsonArray.asScala.to(ParVector)

    /**
     * Shorthand for `el.getAsJsonArray.asScala`
     *
     * @param el `JsonElement` to convert to scala iterable
     * @return iterable of JsonElement
     */
    // def getAsIter(el: JsonElement): Iterable[JsonElement] = el.getAsJsonArray.asScala

    /**
     * Shorthand for `el.getAsJsonArray.asScala.map(_.getAsJsonObject())`
     *
     * @param el `JsonElement` to convert to scala iterable
     * @return iterable of JsonObject
     */
    // def getAsJsonObjIter(el: JsonElement): Iterable[JsonObject] = el.getAsJsonArray.asScala.map(_.getAsJsonObject)

    /**
     * Shorthand for `el.asScala.map(_.getAsJsonObject())`
     *
     * @param el `JsonElement` to convert to scala iterable
     * @return iterable of JsonObject
     */
    def getAsJsonObjIter(el: JsonArray): Iterable[JsonObject] = el.asScala.map(_.getAsJsonObject)

    /*
      @param rawJson String
      @return prettify json string, i.e. indented ...
     */
    // def prettifyJson(rawJson: String) = "" //rawJson.parseJson.prettyPrint

    /**
     * Removes special characters and other that can
     * prevent text from displaying / being read/written properly
     *
     * @param str sring to sanitize
     * @return sanitized string
     */
    def sanitize(str: String): String = str
        .replace("\r", "")
        .replace("\'", "")
    // \n line endings are supported fine on a greater number of platform (including windows) than "\r\n"

    /**
     * Try to apply given function `resolver` if it succeeds => return the result,
     * or if an exception happened => return `defaultVal`
     *
     * @param resolver, function to try
     * @param defaultVal value to return when an exception happened
     * @return see above
     */
    def tryOrElse[T](resolver: Function0[T], defaultVal: T): T = {
        try {
            resolver()
        } catch {
            case e: Exception => {
                e.printStackTrace(errLogPrintWriter)
                errLogPrintWriter.println(sep)
                defaultVal
            }
        }
    }
}
