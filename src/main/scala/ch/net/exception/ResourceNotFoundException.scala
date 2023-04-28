package ch.net.exception

trait ResourceNotFoundException {
    // This is called a "self annotation". You can use "self" or "dog" or whatever you want.
    // It requires that those who extend this trait must also extend Throwable, or a subclass of it.
    self: Throwable =>
}

object ResourceNotFoundException {

    private[exception] def fmtErr(courseYear: String) = {
        val tmp = courseYear.split("-")
        val year = tmp(0)
        val code = tmp(1)
        f"code \"$code\" was not found. Please check spelling and retry."
    }
}

case class CourseNotFoundException(private val message: String) extends Exception(ResourceNotFoundException.fmtErr(message)) with ResourceNotFoundException {

    }
/* object CourseNotFoundException {
    private[exception] def fmtErr(courseYear: String) = {
        val tmp = courseYear.split("-")
        val year = tmp(0)
        val code = tmp(1)
        f"code \"$code\" was not found. Please check spelling and retry."
    }
} */

