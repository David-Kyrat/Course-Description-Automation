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
