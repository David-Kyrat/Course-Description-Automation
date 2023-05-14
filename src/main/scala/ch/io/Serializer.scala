package ch.io

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

    def yamlMultiLineStr(key: String, value: String) = {
        val sbld = new StringBuilder(f"$key:  |\n")
        val indent = " " * (f"$key:  ".length) // indentation to respect to have correct yaml syntax
        sbld ++= indent
        val lines = value.strip().replace("\n", f"\n$indent")
        sbld ++= lines
        /* lines.map(line => f"$line\n$indent") */

        sbld.toString // .stripTrailing()
    }

    def yamlFmtCursus(course: Course) = {
        val map = course.studyPlan
        val sbld = new StringBuilder("cursus:\n")
        val credFmt: (Int) => String = c => if (c <= 0) "\\-" else c.toString // if credits = 0 write a "-" instead
        map.foreach(kv => sbld ++= f"  - {name: ${kv._1}, type: ${kv._2._2}, credits: ${credFmt(kv._2._1)}}\n")
        sbld.toString
    }

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
        val path = Path.of(f"res/md/$name")
        val wr = new PrintWriter(new BufferedWriter(new FileWriter(path.toAbsolutePath.toString, UTF_8)), true)
        // Just returns the Writer at the end
        def write(content: String) = { wr.print(content + "\n") }
        def writes(contents: String*) = for (s <- contents) write(s)

        write(yamlHeaderSep)
        writes(
          yamlFmt("title", course.title),
          yamlFmt("author", course.authors.mkString(", ") + f"  \\-  ${course.id}"),
          yamlFmt("weekly_hours", course.hoursNb.sum),
          yamlFmt("lectures_hours", course.hoursNb.lectures),
          yamlFmt("exercices_hours", course.hoursNb.exercices),
          yamlFmt("practice_hours", course.hoursNb.practice),
          yamlFmt("total_hours", course.hoursNb.semesterSum),
          yamlFmt("course_lang", course.language),
          yamlFmt("semester", course.semester),
          yamlFmt("eval_mode", course.evalMode),
          yamlFmt("exa_session", course.semester.session),
          yamlFmt("course_format", course.format.replace("-", "\\-")),
          yamlFmtCursus(course),
          yamlMultiLineStr("objective", Utils.sanitize(course.objective)),
          yamlMultiLineStr("description", Utils.sanitize(course.description)),
          yamlMultiLineStr("various", Utils.sanitize(course.various)),
          yamlMultiLineStr("comments", Utils.sanitize(course.comments))
        )
        write(yamlHeaderSep)
        wr.flush()
        wr.close
    }

    //
}
