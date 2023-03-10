package ch

sealed trait CourseActivity

case object Cours extends CourseActivity
case object Exercices extends CourseActivity
// their toString methods defaults to "Cours", "Exercices" i.e. what we want to search for when parsing

case object Practice extends CourseActivity {
    override def toString = "Travaux pratiques"
}

object CourseActivity {

    /** Vector containing all the entity implementing
     *  the sealed trait defined in this file */
    val ALL: Vector[CourseActivity] = Vector(Cours, Exercices, Practice)

    /**
     * Map association the string representation of each case object
     * implementing the sealed trait defined here to itself
     */
    val ALL_MAP: Map[String, CourseActivity] = ALL.map(act => (act.toString, act)).toMap

}
