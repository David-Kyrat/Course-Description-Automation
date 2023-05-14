package test

import ch.Helpers._
import ch.StudyPlan
import ch.Utils
import ch.Utils.crtYear
import ch.Utils.pathOf
import ch.Utils.r
import ch.io.Serializer
import ch.net.ReqHdl
import ch.net.Resp
import ch.net.exception._
import com.google.gson.JsonArray
import test.TestCourse
import test.TestCourse._

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import scala.collection.parallel.immutable.ParVector
import scala.jdk.CollectionConverters._

object TestStudyPlan {

    def testCreateAbbrevFile() = {
        StudyPlan.createAbbrevFile()
    }

    def getStudyPlans() = {
        val x = StudyPlan.ALL
        val json = Resp.gson.toJson(x.asJava)
        Utils.write(pathOf(f"all_sp_2022.json"), json)
    }

    def testStudyPlanFactory() = {
        val id = "74813"
        val sp = StudyPlan(id)
        println(sp.id)
        sp.courses.take(10).map(_.toShortString).foreach(println)
    }
  
/*def testGetStudyPlans() = {
        val bs = Using(Source.fromURL(f"${ReqHdl.baseUrl}/$descIpa22"))
        val resp = bs.mkString
        bs.close()
        resp
        val rh: ReqHdl = ReqHdl.g(descIpa22)
        val serverResponse: Resp = rh()
        println(serverResponse)
        // println("\n\n----------------------- Study Plans -----------------------------")
        val studyPlansReq: ReqHdl = studyPlan()
        val studyPlansResp: Resp = studyPlansReq()
        println(studyPlansResp)
}*/
/* def testJsonLib() = {
        val jsonString = Utils.prettifyJson(course(f"${crtYear}-11X001").get())
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
    }

    def testResolveCoursHours() = {
        val courseTest = "11X001"
        val jsObj = Course.get(courseTest, crtYear)
        val coursHours = Course.resolveCourseHours(jsObj)
        println(coursHours)
    } */
}
