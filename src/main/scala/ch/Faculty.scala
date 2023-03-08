package ch

//FIX: MAKE AN ENUM RATHER THAN A CASE CLASS

/**
 * Represents a Faculty, e.g. Sciences
 *
 * @param id, http request id, to get a details about a given faculty
 */
case class Faculty(id: String) {

    val sections: Vector[Section] = ??? // TODO:
    val studyPlans: Vector[StudyPlan] = ??? //TODO:
}
