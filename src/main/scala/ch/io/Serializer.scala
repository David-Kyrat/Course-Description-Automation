package ch.io

import scala.collection.immutable.ArraySeq

import java.io.{BufferedWriter, FileWriter, PrintWriter}
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path

import ch.{Course, Utils}

object Serializer {
    // Begin and end symbol of yaml header in markdown file
    val yamlHeaderSep = "---"

    /**
     * Format given parameters in a yaml fmt i.e. "key: value"
     * @param key String
     * @param value String
     */
    def yamlFmt[T](key: String, value: T): String = f"$key: $value"

    // unused
    def yamlFmTOpt[T](key: String, value: Option[T]) =
        value match {
            case Some(t) => f"$key: $t"
            case None    => ""
        }

    /**
     * Writes optional fields of given course to given writer.
     * i.e. Do nothing if `Option` is None and writes the "regular"
     * serialization on extracted object to `wr` if it is `Some`
     *
     * @param wr `BufferedWriter` to write to
     * @param course course to extract optional fields from
     */
    def yamlWriteCourseOpt(br: BufferedWriter, course: Course) = {
        val keyOptValuePair: ArraySeq[(String, Object)] = ArraySeq(("prerequisite", course.prerequisites), ("various", course.various), ("comments", course.comments))
        keyOptValuePair.map(pair =>
            pair._2 match {
                case Some(value) => write(br, yamlFmtMultiLineStr(pair._1, value.toString))
                case None        => ()
            }
        )
    }

    /**
     * Format given parameters in a yaml fmt i.e. "key: value",
     * where value is a string over several lines (i.e. with '\n' characters in it)
     * @param key String
     * @param value String
     */
    def yamlFmtMultiLineStr(key: String, value: String) = {
        val sbld = new StringBuilder(f"$key:  |\n")
        val indent = " " * (f"$key:  ".length) // indentation to respect to have correct yaml syntax
        sbld ++= indent
        val lines = value.strip().replace("\n", f"\n$indent")
        sbld ++= lines
        sbld.toString
    }

    def yamlFmtCursus(course: Course) = {
        val map = course.studyPlan
        val sbld = new StringBuilder("cursus:\n")
        val credFmt: (Float) => String = c => if (c <= 0) "\\-" else c.toString // if credits = 0 write a "-" instead
        map.foreach(kv => sbld ++= f"  - {name: ${kv._1}, type: ${kv._2._2}, credits: ${credFmt(kv._2._1)}}\n")
        sbld.toString
    }

    /** Buffered writing */
    private def write(br: BufferedWriter, content: String) = { br.write(content + "\n") }

    /** Buffered writing */
    private def writes(br: BufferedWriter, contents: String*) = for (s <- contents) write(br, s)

    /**
     * Serialize a `Course` into a markdown file that can be used to fill
     * the html course-description template.
     *
     * The syntax of those specific markdown file is :
     *  - a yaml header
     *  - empty "body"
     *
     * @param course Course to serialize
     */
    def courseToMarkdown(course: Course) = {
        val name = f"desc-${course.year}-${course.id}.md"
        val path = Utils.pathOf(name)
        val br = new BufferedWriter(new FileWriter(path.toAbsolutePath.toString, UTF_8))
        def write(content: String) = br.write(content + "\n")
        def writes(contents: String*) = for (s <- contents) write(s)

        write(yamlHeaderSep)
        writes(
          yamlFmt("title", course.title),
          yamlFmt("author", course.authors.mkString(", ") + f"  \\-  ${course.id}"),
          yamlFmt("weekly_hours", course.hoursNb.sum),
          yamlFmt("lectures_hours", course.hoursNb.lectures),
          yamlFmt("exercices_hours", course.hoursNb.exercices),
          yamlFmt("total_hours", course.hoursNb.semesterSum),
          yamlFmt("course_lang", course.language),
          yamlFmt("semester", course.semester),
          yamlFmt("eval_mode", course.evalMode),
          yamlFmt("exa_session", course.semester.session),
          yamlFmt("course_format", course.format.replace("-", "\\-")),
          yamlFmtCursus(course),
          yamlFmtMultiLineStr("objective", Utils.sanitize(course.objective)),
          yamlFmtMultiLineStr("description", Utils.sanitize(course.description))
        )
        val ch = course.hoursNb
        if (ch.seminaire > 0) {
            if (ch.practice > 0) write(yamlFmt("practice_hours", course.hoursNb.practice))
            val toWrite = yamlFmt("sem_hours", course.hoursNb.seminaire)
            write(toWrite)
        } else write(yamlFmt("practice_hours", course.hoursNb.practice))
        write(yamlHeaderSep)
        br.flush
        br.close
    }
}
