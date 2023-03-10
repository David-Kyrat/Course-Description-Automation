package ch

sealed trait ExaSession

/**
 * January/Feburary Exam Session
 */
case object Jan extends ExaSession

/**
 * June/Jully Exam Session
 */
case object Jul extends ExaSession

/**
 * Makeup Exam Session. i.e. August/September exam session.
 */
case object Aug extends ExaSession

object ExaSession {

    /** Vector containing all the entity implementing the sealed trait defined in this file */
    val ALL: Vector[ExaSession] = Vector(Jan, Jul, Aug)

    /**
     * Map associating the string representation of each case object implementing the sealed trait defined in this file to itself
     */
    val ALL_MAP: Map[String, ExaSession] = ALL.map(exaSess => (exaSess.toString, exaSess)).toMap
}
