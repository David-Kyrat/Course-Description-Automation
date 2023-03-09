package ch

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
final case class CourseHours(lectures: Int, exercices: Int, practice: Int)

object CourseHours {

    /**
     * Builder for CourseHours
     *
     * @param lectures
     * @param exercices
     * @param practice
     */
    case class CourseHoursBuilder(var lectures: Int = 0, var exercices: Int = 0, var practice: Int = 0) {

        /** @return New immutable CoursHours instance */
        def build() = CourseHours(lectures, exercices, practice)
    }

}

sealed trait CourseActivity

case object Cours extends CourseActivity
case object Exercices extends CourseActivity
case object Practice extends CourseActivity {
    override def toString = "Travaux pratiques"
}

object CourseActivity {
    val ALL = Vector(Cours, Exercices)
    val ALL_MAP: Map[String, CourseActivity] = ALL.map(act => (act.toString(), act)).toMap
}

// their toString methods defaults to "Cours", "Exercices" i.e. what we want
