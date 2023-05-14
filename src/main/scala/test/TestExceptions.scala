package test

import ch.net.exception._

object TestExceptions {
    def testCourseNotFound() = {
        throw new CourseNotFoundException("20223-12X000")
    }

    def testStudyPlanNotFound() = {
        throw new StudyPlanNotFoundException("BMI")
    }

}
