package ch

sealed trait CourseType

case object Mandatory extends CourseType {
    override def toString = "Obligatoire"
}

case object Optional extends CourseType {

    override def toString = "Optionnel"
}

object CourseType {

    /** Vector containing all the entity implementing
     *  the sealed trait defined in this file */
    val ALL: Vector[CourseType] = Vector(Mandatory, Optional)

    /**
     * Map association the string representation of each case object
     * implementing the sealed trait defined here to itself
     */
    val ALL_MAP: Map[String, CourseType] = ALL.map(cType => (cType.toString, cType)).toMap
}
