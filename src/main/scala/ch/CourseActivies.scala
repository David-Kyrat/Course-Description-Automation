package ch

sealed trait CourseActivity

case object Cours extends CourseActivity
case object Exercices extends CourseActivity
case object Practice extends CourseActivity {
    override def toString = "Travaux pratiques"
}

object CourseActivity {
    val ALL = Vector(Cours, Exercices, Practice)
    val ALL_MAP: Map[String, CourseActivity] = ALL.map(act => (act.toString(), act)).toMap

}


// their toString methods defaults to "Cours", "Exercices" i.e. what we want
