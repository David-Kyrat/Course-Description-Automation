package ch

import scala.collection.immutable
import scala.jdk.CollectionConverters._
import scala.sys.env
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

import java.io.File
// import scala.language.postfixOps
import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Paths
import java.nio.file.StandardOpenOption._
import java.nio.file.attribute.FileAttribute
import java.nio.file.{Files, Path}
import java.time.LocalDate
import java.time.format.DateTimeFormatter

final object Utils {
    val LOG_MAX_SIZE = 50 << 20 // 5MB
    val LOG_NAME = "cda-err.log"

    private lazy val logFile_Try: Try[File] = getLogPath
    lazy val logFile: File = logFile_Try match {
        case Success(file) => if (file.exists) file else logFallBack()
        case Failure(e)    => logFallBack()
    }
    if (logFile.canRead()) logFile.setReadable(true, false)
    if (logFile.canWrite()) logFile.setWritable(true, false)
    lazy val canLog = true

    /**
     * FallBack if usual log file isnt found. Creates a file at `$HOME/Documents/err.log`
     *
     * @return `java.io.File` pointing to created (or already existing) fallback logfile
     */
    private def logFallBack(): File = {
        val logPath = Path.of(env("HOME"), "Documents", LOG_NAME)
        Try { Files.createFile(logPath) } // now either this try failed and the file already existed or it didn't and the Try succeeded in both case the file exists after this line
        logPath.toFile()
    }

    /** @return current running directory (variable set by the JVM) */
    def cwd: String = System.getProperty("user.dir")

    private def logStartMsg: String = String.format("\n\n[%s]: --------------------------------------- Run started %s \tcurrent path: %s \n", now(), sep, cwd)
    private val sep = "---------------------------------------\n\n"

    private lazy val logWrtr: PrintWriter = if (canLog) {
        // val fw = new FileWriter() logPath)
        val pw = new PrintWriter(new BufferedWriter(new FileWriter(logFile, UTF_8, true)), true)
        pw.println(logStartMsg)
        pw
    } else null

    private def getLogPath: Try[File] = Try {
        val path = pathOf(f"log/$LOG_NAME") // .toAbsolutePath
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
        if (!path.toFile.exists())
            throw new IllegalStateException("getLogPath: creating parent directories of logFile or logFile itself failed. Falling back to $HOME/Documents/err.log")
        path.toFile()
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

    def createResDirsIfNotExists() = {
        val paths = pathOf("md") :: pathOf("pdf") :: Nil;
        paths.map(_.toAbsolutePath.normalize).foreach(Files.createDirectories(_))
        paths
            .map(_.toAbsolutePath.normalize)
            .foreach(p => {
                log("creating " + p)
                println("creating " + p)
                println("exists ? " + Files.exists(p))
            })
    }

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

    def registerAtExit(f: => Unit): Unit = {
        sys.addShutdownHook(new Thread() {
            override def run(): Unit = {
                try {
                    f
                } catch {
                    case NonFatal(e) =>
                    // ignore errors
                }
            }
        })
    }

    def closeLogWriter(): Unit = {
        if (logWrtr != null) { logWrtr.flush(); logWrtr.close() }
    }

    registerAtExit(closeLogWriter)
}
