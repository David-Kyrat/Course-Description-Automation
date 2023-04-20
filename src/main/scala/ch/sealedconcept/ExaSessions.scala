package ch.sealedconcept

sealed trait ExaSession

/**
 * January/Feburary Exam Session
 */
case object Jan extends ExaSession {
    override def toString = "Janvier"
}

/**
 * June/Jully Exam Session
 */
case object Jul extends ExaSession {
    override def toString = "Juillet"
}

/**
 * Makeup Exam Session. i.e. August/September exam session.
 */
case object Aug extends ExaSession {
    override def toString = "Août"
}

object ExaSession extends SealedConceptObject[ExaSession] {

    /** not needed because infered from `Semester` */
    override def jsonKey = ""  
    override def ALL = Vector(Jan, Jul, Aug)
}
