package test

import scala.jdk.CollectionConverters._
// import scala.collection.parallel.CollectionConverters._

import ch.Helpers._
import ch.Utils.pathOf
import ch.net.Resp
import ch.net.exception._
import ch.{Main, StudyPlan, Utils}

import scala.collection.parallel.immutable.ParSet

object TestStudyPlan {

    def testCreateAbbrevFile() = {
        StudyPlan.createAbbrevFile()
    }

    def getStudyPlans() = {
        val x = StudyPlan.ALL
        val json = Resp.gson.toJson(x.asJava)
        Utils.write(pathOf(f"all_sp_2022.json"), json)
    }

    def testAbbrevMap() = {
        val abbrevMap = Main.abbrevMap
        // println(abbrevMap("BSI"))
        abbrevMap.values.foreach(kv => println(String.format("{\n\tid : %s\n\tname: %s\n}", kv._1, kv._2)))
    }

    def testStudyPlanFactory() = {
        val id = 73722
        val sp = StudyPlan(id)
        sp.courses.map(_.toShortString).foreach(println)
    }

    def testSaveStudyPlanToMarkdown() = {
        val id = 73722
        val sp = StudyPlan(id)
        sp.saveToMarkdown()
    }

    def testMultipleStudyPlanToMarkdown() = {
        val codes = ParSet(73722, 73726, 76324, 74813)
        // ParSet("73722", "73726", "76324", "74813").foreach(StudyPlan(_).saveToMarkdown)
        codes.foreach(code => {
            // println(f"Building study plan $code")
            val sp = StudyPlan(code)
            // println(f"converting $code to markdown")
            sp.saveToMarkdown()
            // println(f"> $code done.\n-------\r\n")
        })
    }
}
    /*  def testGetStudyPlans() = {
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
/*  def testJsonLib() = {
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
    }
*/
