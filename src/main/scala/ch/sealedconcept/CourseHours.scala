package ch.sealedconcept

/**
 * Case class wrapping a tripple of "amount of time"
 * that will determine "how much" of each class a student has per week.
 * i.e. each student taking a course `c` will have `lectures` hours of theory
 * `exercices` hours of exercices sets/correction and `practice` hours per week.
 * they vary based what `c` is.
 *
 * @param lectures   (corresponds to `sealedconcept.Lectures`)
 * @param exercices  (corresponds to `sealedconcept.Exercices`)
 * @param practice  (corresponds to `sealedconcept.Practice`)
 */
final case class CourseHours(lectures: Int, exercices: Int, practice: Int) {
    override def toString = f"{lectures: $lectures, exercices: $exercices, practice: $practice}"

    /** sum of 3 fields. i.e. total nb of hours per week */
    def sum = lectures + exercices + practice

    /** sum throughout the whole semester */
    def semesterSum = sum * 14 // 14 weeks in a semester

    /** @return  format of course i.e. "Course/Exercices" / "Course/Exercices/TP" */
    def getFormat: String = {
        val sb = new StringBuilder()
        val vec = Vector("cours", "exercices", "TP")
        if (lectures > 0) sb ++= "Cours"
        if (exercices > 0) sb ++= (if (sb.isEmpty) "Exercices" else ", exercices")
        if (practice > 0) sb ++= (if (sb.isEmpty) "TP" else ", TP")
        if (sb.isEmpty) "-" else sb.toString
    }
}

object CourseHours {

    /**
     * Watch out, use `getAsJsonArray("activities")` first before
     * calling `get("duration").getAsString`
     *
     * @return json "hierarchy" in which to fetch the required data
     */
    def jsonKey = "activities"
    def jsonKey2 = "duration"

    /** Builder for CourseHours */
    class CourseHoursBuilder(var lectures: Int = 0, var exercices: Int = 0, var practice: Int = 0) {

        /** @return New immutable CoursHours instance */
        def build() = CourseHours(lectures, exercices, practice)

        /**
         * Setter taking a `sealedconcept.CourseActivity` case object as argument
         * and sets the corresponding number of weekly hours accordingly (with pattern matching on the argument of type T, which is doable since CourseActivity is sealed)
         *
         * @param courseActivity, courseActivity that tells which field to modify
         * @param hours, new value to assign
         */
        def setActivity[T >: CourseActivity](courseActivity: T, hours: Int): CourseHoursBuilder = {
            courseActivity match {
                case Lectures  => this.lectures = hours
                case Exercices => this.exercices = hours
                case Practice  => this.practice = hours
            }
            return this
        }
    }

}
