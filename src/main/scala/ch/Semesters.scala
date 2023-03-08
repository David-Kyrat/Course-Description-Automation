package ch

import ch.ExaSession

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
}

case object Autumn extends Semester {

    /**
     * Autumn course always have their exam session
     *  during the January/February Exam Session
     */
    val session: ExaSession = Jan
}

case object Spring extends Semester {

    /**
     * Spring course always have their exam session
     *  during the June/Jully Exam Session
     */
    val session: ExaSession = Jul
}

// not actually "one" semester but it doesn't matter in our implementation
case object Yearly extends Semester {

    /**
     * Yearly course always have their exam session
     *  during the June/Jully Exam Session
     */
    val session: ExaSession = Jul
}
