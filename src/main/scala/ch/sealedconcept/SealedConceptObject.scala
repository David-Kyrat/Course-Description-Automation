package ch.sealedconcept

/**
 * Represents companion object of sealed trait representing a fixed concept (e.g. Semesters, CourseType...)
 * @param T type of sealed trait
 */
trait SealedConceptObject[T] {

    /**
     * Vector containing all the entity implementing
     *  the sealed trait defined in the file where this is implemented
     */
    def ALL: Vector[T]

    /**
     * Map association the string representation of each case object
     * implementing the sealed trait defined here to itself
     */
    def ALL_MAP: Map[String, T] = ALL.map(t => (t.toString(), t)).toMap
}
