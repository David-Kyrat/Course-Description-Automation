package ch.sealedconcept

/**
 * Case class wrapping a tripple of "amount of time"
 * that will determine "how much" of each class a student has per week.
 * i.e. each student taking a course `c` will have `lectures` hours of theory
 * `exercices` hours of exercices sets/correction and `practice` hours per week.
 * they vary based what `c` is.
 *
 * @param lectures
 * @param exercices
 * @param practice
 */
final case class CourseHours(lectures: Int, exercices: Int, practice: Int) {
    override def toString = f"{lectures: $lectures, exercices: $exercices, practice: $practice}"
}

object CourseHours {

    /**
     * Watch out, use `getAsJsonArray("activities")` first before
     * calling `get("duration").getAsString`
     *
     * @return json "hierarchy" in which to fetch the required data
     */
    def jsonKey = "activities.duration"

    /** Builder for CourseHours */
    class CourseHoursBuilder(var lectures: Int = 0, var exercices: Int = 0, var practice: Int = 0) {

        /** @return New immutable CoursHours instance */
        def build() = CourseHours(lectures, exercices, practice)
    }

}
