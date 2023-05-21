package ch.sealedconcept

sealed trait CourseActivity

case object Lectures extends CourseActivity {
    override def toString = "Cours"
}
case object Exercices extends CourseActivity
// their toString methods defaults to "Cours", "Exercices" i.e. what we want to search for when parsing

case object Practice extends CourseActivity {
    override def toString = "Travaux pratiques"
}
case object Seminaire extends CourseActivity {
    override def toString = "Séminaire"
}

case object CoursSeminaire extends CourseActivity { override def toString = "Cours-séminaire"}

object CourseActivity extends SealedConceptObject[CourseActivity] {

    /**
     * Watch out, use `getAsJsonArray("activities")` first before
     * calling `get("type").getAsString`
     *
     * @return json "hierarchy" in which to fetch the required data
     */
    override def jsonKey = "activities"
    def jsonKey2 = "type"

    /**
     * Vector containing all the entity implementing
     *  the sealed trait defined in this file
     */
    override def ALL = Vector(Lectures, Exercices, Practice, Seminaire, CoursSeminaire)
}
