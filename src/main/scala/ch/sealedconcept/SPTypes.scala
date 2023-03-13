package ch.sealedconcept

import ch.sealedconcept.SealedConceptObject
/**
 * Study Plan Type, i.e. bachelor, master, phd
 */
sealed trait SPType

case object Bachelor extends SPType
case object Master extends SPType
case object Phd extends SPType
case object Other extends SPType //NOTE: Placeholder for other types that may have been forgotten at time of writing

object SPType extends SealedConceptObject[SPType] {
    def ALL = Vector(Bachelor, Master, Phd, Other)
}
