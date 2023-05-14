package ch

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption._
import java.time.LocalDate
import java.time.LocalDateTime

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Some utility functions
 */
final object Utils2 {
    val LOG_MAX_SIZE = 1000000 // 1MB
    private val sep = "---------------------------------------\n\n"

    private val gson: Gson = new GsonBuilder().setPrettyPrinting().create()
    private val logPath_Try = getLogPath
    private val logPath = logPath_Try match {
        case Success(path) => path
        case Failure(e)    => Path.of("")
    }

    val canLog = logPath_Try.isSuccess
    private val errLogPrintWriter = if (canLog) new PrintWriter(new BufferedWriter(new FileWriter(logPath.toString, UTF_8, true)), true) else null
    if (canLog) errLogPrintWriter.println(String.format("[%s]: --------------------------------------- Run started %s"), LocalDateTime.now(), sep)

    private def getLogPath: Try[Path] = Try {
        val path = Path.of("res/log/err.log").toAbsolutePath
        val exists = Files.exists(path)
        if (!exists || Files.size(path) > LOG_MAX_SIZE) {
            if (exists) Files.delete(path) // delete logs if the'yre too big
            Files.createDirectories(path.getParent)
            Files.createFile(path)
        }
        path
    }

    def read(path: Path) = String.join("\n", Files.readAllLines(path, UTF_8))
    def write(path: Path, content: String, append: Boolean = false) = {
        val opt = if (append) APPEND else TRUNCATE_EXISTING
        Files.writeString(path, content, UTF_8, CREATE, opt)
    }

    /**
     * Writes the given message to the log file located at `res/log/err.log`
     * if there were no error getting/creating it.
     * Otherwise, do nothing.
     * @param msg Message to log
     */
    def log(msg: String) = { if (canLog) errLogPrintWriter.println(msg + "\n") }

    /**
     * @return Lastest version for course & study plan information
     * i.e. current year - 1
     */
    def crtYear: Int = LocalDate.now.getYear - 1

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
