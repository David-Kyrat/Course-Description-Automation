package ch.net.exception

case class CourseNotFoundException(private val message: String)
        extends Exception(ResourceNotFoundException.fmtErr("course \"" + message.split("-")(1) + "\"")) with ResourceNotFoundException
