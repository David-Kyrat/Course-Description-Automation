package ch.net.exception

class CourseNotFoundException private (s: String) extends Exception(s) {}

object CourseNotFoundException {
    private[exception] def fmtErr(courseYear: String) = {
        val tmp = courseYear.split("-")
        val year = tmp(0)
        val code = tmp(1)
        f"code \"$code\" was not found. Please check spelling and retry."
    }
}
