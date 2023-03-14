package ch.io

import ch.Course

import ch.sealedconcept.{SealedConceptObject, CourseType, CourseHours, Semester, CourseActivity, Lectures, Exercices, Practice}
import java.nio.charset.StandardCharsets.UTF_8
import java.io.{PrintWriter, FileWriter}
import java.nio.file.Path
import java.io.BufferedWriter
import scala.io.Source
import ch.Utils

object Serializer {
    // Begin and end symbol of yaml header in markdown file
    val yamlHeaderSep = "---"

    /**
     * Format given parameters in a yaml fmt i.e. "key: value"
     * @param key String
     * @param value String
     */
    def yamlFmt[T](key: String, value: T): String = f"$key: $value"

    def yamlFmt[T](key: String, values: Seq[T]): String  = ??? //TODO:

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
        val path = Path.of("res/test-auto-desc.md")
        // FIX: Search in old projects how optimized Writer works
        val wr = new BufferedWriter(new FileWriter(Path.of("res/test-auto-desc.md").toAbsolutePath.toString, UTF_8, true))
        // Just returns the Writer at the end
        /* def write(content: String) = { wr.write(content); wr }
        def writes(contents: String*) = {
            for (s <- contents) {
                wr.write(s)
                wr.newLine()
            }
        } */

        Utils.write(path, yamlHeaderSep + "\n")
        Utils.writes(
          path,
          yamlFmt("title", course.title),
          yamlFmt("author", course.authors.mkString(", ")),
          yamlFmt("weekly_hours", course.hoursNb.sum),
          yamlFmt("wekkly_exo_hours", course.hoursNb.exercices),
          yamlFmt("wekkly_practice_hours", course.hoursNb.practice),
          yamlFmt("total_hours", course.hoursNb.semesterSum),
          yamlFmt("course_lang", course.language),
          yamlFmt("semester", course.semester),
          yamlFmt("eval_mode", course.evalMode),
          yamlFmt("exa_session", course.semester.session),
          yamlFmt(
            "course_format",
            course.format match {
                case Some(f) => f
                case None    => ""
            }
          )
          // yamlFmt("cursus", ), //TODO: serialize yaml map
        )
        Utils.write(path, yamlHeaderSep + "\n", true)
    }

    //
}
