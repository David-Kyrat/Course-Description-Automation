package test

import ch.net.exception._

object TestExceptions {
    def testCourseNotFound() = {
        val x = new CourseNotFoundException("20223-12X000")
        println(x)
    }

    def testStudyPlanNotFound() = {
        val y = new StudyPlanNotFoundException("BMI")
        println(y)
    }

}
