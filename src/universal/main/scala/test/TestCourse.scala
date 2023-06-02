package test

import scala.collection.parallel.immutable.ParVector

import java.io.File
import java.nio.file.{Files, Path}

import ch.Course._
import ch.Utils.crtYear
import ch.io.Serializer
import ch.net.{ReqHdl, Resp}
import ch.{Course, StudyPlan, Utils}

object TestCourse {

    def writeCoursDescToRes(id: String, year: Int = crtYear) = Utils.write(Utils.pathOf(f"$id-desc.json"), Resp.prettify(Course.get(id)))
    def testNext() = {
        val ipa22Id = f"${crtYear}-11X001"
        val ipa22Req = ReqHdl.course(f"$ipa22Id?size=1000")
        val ipa22Resp = ipa22Req()
        val next = ipa22Resp.next(26)
        // Utils.write(Path.of("res/out.json"), ipa22Resp)
        // println(ipa22Req.get())
        // println(ipa22Req.next().get())
        println(next)
        println(f"\n$next")
    }

    def testCourseFactoryMethod() = {
        val course = Course("11X001", crtYear)
        println(course)
        println("-------------------------------------------------\n\n\n")
        val course2 = Course("12M040", crtYear)
        // Utils.write(Path.of("desc.txt"), Utils.sanitize(course2.description))
        println(course2)
    }

    def testCourseToMarkdown() = {
        val code = "11X001"
        println(f"Building course $code")
        val course = Course(code, crtYear)
        println("converting to markdown")
        course.saveToMarkdown()
        println("> Done.")
    }

    def testMultipleCourseToMarkdown() = {
        val codes = ParVector("12M040", "11X001", "13M016A", "14M252", "12X050", "14P017")
        // val codes = ParVector("12M04", "11X01", "13016A", "14M52", "12X50", "14017")
        codes.foreach(code => {
            println(f"Building course $code")
            val course = Course(code)
            println(f"converting $code to markdown")
            course.saveToMarkdown()
            println(f"> $code done.\n-------\r\n")
        })
    }

    def testCourseOptional() = {
        val codes = Vector("12M040", "11X001", "13M016A", "14M252", "12X050", "14P017")
        codes.foreach(code => {
            println(f"Building course $code")
            val course = Course(code)
            val triple = (course.prerequisites, course.listenerAccepted, course.publicAccepted)
            println(f"preqreq:\n\t${triple._1}")
            println(f"list_accepted:\n\t${triple._2}")
            println(f"public_accepted:\n\t${triple._2}")
            // writeCoursDescToRes(code)
            println(f"> $code done.\n-------\r\n")
        })
    }
}

/* def testJsonLib() = {
        val jsonString = Utils.prettifyJson(Course(f"${crtYear}-11X001").get())
        val jsObj: JsonObject = new Gson().fromJson(jsonString, classOf[JsonObject])
        val ye = jsObj.get("academicalYear")
        val v1 = jsObj.get("code")
        val v2 = "activities"
        val activities: JsonArray = jsObj.getAsJsonArray(v2)
        val lectures: JsonObject = activities.get(0).getAsJsonObject()
        val v3 = lectures.get("title")
        val v4 = lectures.get("duration")
        val v5 = lectures.get("periodicity")
        val v6 = lectures.get("objective")
        val v7 = lectures.get("intended")
        val v8 = lectures.get("variousInfo")
        val v9 = lectures.get("comment")
        val v0 = lectures.get("type")

        val vec = Vector(ye, v1, v2, v3, v4, v5, v6, v7, v8, v9, v0)
        activities.forEach(el => println(el.getClass))
    } */

/* def testResolveCoursHours() = {
        val courseTest = "11X001"
        val jsObj = Course.get(courseTest, crtYear)
        val coursHours = Course.resolveCourseHours(jsObj) // NB: Do not decomment method is now private
        println(coursHours)
    } */
