package ch.sealedconcept

/**
 * Study Plan Type, i.e. bachelor, master, phd
 */
sealed trait SPType

case object Bachelor extends SPType
case object Master extends SPType
case object Phd extends SPType

object SPType extends SealedConceptObject[SPType] {

    case object Other extends SPType // NOTE: Placeholder for other types that may have been forgotten at time of writing
    override def jsonKey = "studyLevel" // NOTE: field only available in GET Request for studyPlan
    override def ALL = Vector(Bachelor, Master, Phd, Other)
}
