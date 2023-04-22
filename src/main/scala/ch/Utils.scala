package ch

import com.google.gson.{Gson, GsonBuilder}
import spray.json._

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import java.time.LocalDate
import scala.language.postfixOps
//import DefaultJsonProtocol._
import java.io.{BufferedWriter, FileWriter, PrintWriter}

final object Utils {
    private val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
    private val logPath = Path.of("res/log/err.log").toAbsolutePath
    write(logPath, "") // prevents logfile content from getting to big by cleaning it
    private val errLogPrintWriter = new PrintWriter(new BufferedWriter(new FileWriter(logPath.toString, UTF_8, true)), true)
    private val sep = "---------------------------------------\n\n"

    def read(path: Path) = String.join("\n", Files.readAllLines(path, UTF_8))
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
     * @param rawJson String
     * @return prettify json string, i.e. indented ...
     */
    def prettifyJson(rawJson: String) = rawJson.parseJson.prettyPrint

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
