package ch.sealedconcept

sealed trait CourseActivity

case object Cours extends CourseActivity
case object Exercices extends CourseActivity
// their toString methods defaults to "Cours", "Exercices" i.e. what we want to search for when parsing

case object Practice extends CourseActivity {
    override def toString = "Travaux pratiques"
}

object CourseActivity extends SealedConceptObject[CourseActivity] {
    /**
     * Vector containing all the entity implementing
     *  the sealed trait defined in this file
     */
    override def ALL = Vector(Cours, Exercices, Practice)
}
