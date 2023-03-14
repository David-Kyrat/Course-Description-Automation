package ch

// import ch.Resp._
import ch.ReqHdl._
import ch.Utils

import com.google.gson.{JsonElement, JsonParser, JsonSerializer, JsonDeserializer}
import java.nio.file.Path
import com.google.gson.JsonObject
import com.google.gson._
import com.google.gson.JsonObject._
import com.google.gson.Gson
import ch.sealedconcept.CourseHours
import ch.sealedconcept.Lectures
import ch.sealedconcept.Exercices
import ch.sealedconcept.Practice
import ch.io.Serializer

object Main {
    def testGetStudyPlans() = {
        /*val bs = Using(Source.fromURL(f"${ReqHdl.baseUrl}/$descIpa22"))
        val resp = bs.mkString
        bs.close()
        resp
        val rh: ReqHdl = ReqHdl.g(descIpa22)
        val serverResponse: Resp = rh()
        println(serverResponse) */
        // println("\n\n----------------------- Study Plans -----------------------------")
        val studyPlansReq: ReqHdl = studyPlan()
        val studyPlansResp: Resp = studyPlansReq()
        println(studyPlansResp)
    }

    def writeCoursDecsToRes(id: String, year: Int) = {
        Utils.write(Path.of(f"res/$id-desc.json"), ReqHdl.course(f"$year-$id").get())
    }

    def testNext() = {
        val ipa22Id = "2022-11X001"
        val ipa22Req = ReqHdl.course(f"$ipa22Id?size=1000")
        val ipa22Resp = ipa22Req.get()
        val next = ipa22Req.next(26)
        // Utils.write(Path.of("res/out.json"), ipa22Resp)
        // println(ipa22Req.get())
        // println(ipa22Req.next().get())
        println(next.get())
        println(f"\n$next")
    }
    def testJsonLib() = {
        val jsonString = Utils.prettifyJson(course("2022-11X001").get())
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
        val v0 = lectures.get("type") // NOTE: WORKS !

        val vec = Vector(ye, v1, v2, v3, v4, v5, v6, v7, v8, v9, v0)
        activities.forEach(el => println(el.getClass))
    }

    def testResolveCoursHours() = {
        val jsObj = Course.get("12M040", 2022)
        /* val coursHours = Course.resolveCourseHours(jsObj)
        println(coursHours) */
    }

    def testCourseFactoryMethod() = {
        val course = Course("11X001", 2022)
        println(course)
        println("-------------------------------------------------\n\n\n")
        val course2 = Course("12M040", 2022)
        //Utils.write(Path.of("desc.txt"), Utils.sanitize(course2.description))
        println(course2)
    }

    def testCourseToMarkdown() = {
        val course = Course("12M040", 2022)
        Serializer.courseToMarkdown(course)
    }

    def main(args: Array[String]): Unit = {
        println("\n\n")
        // writeCoursDecsToRes("11X001", 2022)
        // testJsonLib()
        // testResolveCoursHours()
        // testCourseFactoryMethod()
        testCourseToMarkdown()

        println("\n\n")
    }
}
