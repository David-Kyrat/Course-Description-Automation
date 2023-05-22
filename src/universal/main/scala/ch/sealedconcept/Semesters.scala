package ch.sealedconcept

/**
 * Represents a period of time in which a course is given
 * i.e. Autumn, Spring, Yearly
 */
sealed trait Semester {

    /**
     * Makeup exams are always during the August/September
     *  exam session
     */
    val makeupSession: ExaSession = Aug
    def session: ExaSession
}

case object Autumn extends Semester {

    /**
     * Autumn course always have their exam session
     *  during the January/February Exam Session
     */
    override def session: ExaSession = Jan
    override def toString = "Automne"
}

case object Spring extends Semester {

    /**
     * Spring course always have their exam session
     *  during the June/Jully Exam Session
     */
    override def session: ExaSession = Jul
    override def toString = "Printemps"
}

// not actually "one" semester but it doesn't matter in our implementation
case object Yearly extends Semester {

    /**
     * Yearly course always have their exam session
     *  during the June/Jully Exam Session
     */
    override def session: ExaSession = Jul
    override def toString = "Annuel"
}

object Semester extends SealedConceptObject[Semester] {

    /**
     * Watch out, use `getAsJsonArray("activities")` first before
     * calling `get("periodicity").getAsString`
     *
     * @return json "hierarchy" in which to fetch the required data
     */
    override def jsonKey = "activities"
    def jsonKey2 = "periodicity"
    override def ALL = Vector(Autumn, Spring, Yearly)
}
