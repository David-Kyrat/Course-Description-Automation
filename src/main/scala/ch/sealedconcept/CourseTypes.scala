package ch.sealedconcept

sealed trait CourseType

case object Mandatory extends CourseType {
    override def toString = "Obligatoire"
}

case object Optional extends CourseType {
    override def toString = "Optionnel"
}

object CourseType extends SealedConceptObject[CourseType] {
    override def ALL = Vector(Mandatory, Optional)
}
