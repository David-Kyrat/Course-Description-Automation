package ch.net.exception

case class StudyPlanNotFoundException(private val studyPlanName: String)
        extends Exception(ResourceNotFoundException.fmtErr(f"study plan \"$studyPlanName\"")) with ResourceNotFoundException
