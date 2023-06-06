package ch

import scala.collection.immutable
import scala.jdk.CollectionConverters._
// import scala.language.postfixOps
import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

final object Utils {
    val LOG_MAX_SIZE = 5 << 20 // 5MB
    private lazy val logPath_Try = getLogPath
    lazy val logPath = logPath_Try match {
        case Success(path) => path
        case Failure(e)    => Path.of("")
    }
    lazy val canLog = logPath_Try.isSuccess

    private lazy val logWrtr: PrintWriter = if (canLog) {
        val pw = new PrintWriter(new BufferedWriter(new FileWriter(logPath.toString, UTF_8, true)), true)
        pw.println(String.format("\n\n[%s]: --------------------------------------- Run started %s", now(), sep))
        pw
    } else null
    private val sep = "---------------------------------------\n\n"

    private def getLogPath: Try[Path] = Try {
        val path = pathOf("log/err.log") // .toAbsolutePath
        var exists = Files.exists(path)
        // println("LogPath: " + path.toAbsolutePath)
        // to prevent call Files.sizes 2 times => separate conditions
        if (exists && Files.size(path) >= LOG_MAX_SIZE) {
            Files.delete(path)
            exists = false
        }
        if (!exists) {
            Files.createDirectories(path.resolve("..").normalize())
            Files.createFile(path)
        }
        path
    }

    /**
     * Shorthand for custom datetime format
     * @return Current DateTime timestamp
     */
    def now(): String = java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("dd/MM/YYYY - HH:mm:ss"))

    /**
     * Resolve given 'against' resource path
     * e.g. if we want to acces `/files/res/md/test.md`
     * just enter `md/test.md`.
     * And this function will return the corresponding relative path
     * @param path resource to locate
     * @return valid relative to resource (relative w.r.t the runnable i.e. jar or else)
     */
    def r(path: String): String = f"files/res/$path"
    // def r(path: String): String = f"res/$path"
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
    lazy val crtYear: Int = LocalDate.now.getYear - 1

    /**
     * Define format / i.e. additional information that will be added to each entry in log file
     * (e.g. date & time etc...)
     *
     * @param msg Message to log
     */
    private def fmtLog(msg: String) = String.format("[%s]:  %s", now, msg)

    /**
     * Writes the given message to the log file located at `res/log/err.log`
     * if there were no error getting/creating it.
     * Otherwise, do nothing.
     * @param msg Message to log
     */
    def log(msg: String) = {
        if (canLog) {
            try { logWrtr.println(fmtLog(msg)) }
            catch { case _: Throwable => () }
        }
    }

    /**
     * Writes the `stackTrace` of the given message to the log file located at `res/log/err.log`
     * if there were no error getting/creating it.
     * Otherwise, do nothing.
     * @param err `Exception` to get the stackTrace from
     * @param additionalMsg additional Message to add at the top of the stackTrace
     */
    def log[T <: Throwable](err: T, additionalMsg: String = "") = {
        if (canLog) {
            try {
                // println(String.format("msg: %s", additionalMsg))
                logWrtr.println(fmtLog(f"Exception occured. Additional Message \"${additionalMsg}\"\n---"))
                err.printStackTrace(logWrtr)
                logWrtr.println()
            } catch { case _: Throwable => () }
            // } catch { case t: Exception => (t.printStackTrace())) }
        }
    }

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
     * @param additionalMsg additional Message to add at the top of the stackTrace
     * @param logErr whether to log the error
     * @return see above
     */
    def tryOrElse[T](resolver: Function0[T], defaultVal: T, additionalMsg: String, logErr: Boolean): T = {
        try {
            resolver()
        } catch {
            case e: Exception => {
                if (canLog && logErr) log(e, additionalMsg)
                defaultVal
            }
        }
    }

    /**
     * Try to apply given function `resolver` if it succeeds => return the result,
     * or if an exception happened => return `defaultVal`
     *
     * @param resolver, function to try
     * @param defaultVal function that returns a default value when an exception happened
     * @param additionalMsg additional Message to add at the top of the stackTrace
     * @param logErr whether to log the error
     * (NB: it's important to pass in a function otherwise the default value will be computed when this method is called)
     *
     * @return see above
     */
    def tryOrElse[T](resolver: Function0[T], defaultVal: () => T, additionalMsg: String = "", logErr: Boolean = true): T = {
        try {
            resolver()
        } catch {
            case e: Exception => {
                if (canLog && logErr) log(e, additionalMsg)
                defaultVal()
            }
        }
    }
}
