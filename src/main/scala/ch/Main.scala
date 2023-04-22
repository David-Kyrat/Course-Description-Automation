package ch

// import ch.Resp._
import ch.Utils.crtYear
import ch.io.Serializer

import java.io.File
import java.nio.file.{Files, Path}
import scala.collection.parallel.immutable.ParVector

object Main {
    def writeCoursDecsToRes(id: String, year: Int = crtYear) = Utils.write(Path.of(f"res/$id-desc.json"), ReqHdl.course(f"$year-$id").get())

    def testNext() = {
        val ipa22Id = f"${crtYear}-11X001"
        val ipa22Req = ReqHdl.course(f"$ipa22Id?size=1000")
        val ipa22Resp = ipa22Req.get()
        val next = ipa22Req.next(26)
        // Utils.write(Path.of("res/out.json"), ipa22Resp)
        // println(ipa22Req.get())
        // println(ipa22Req.next().get())
        println(next.get())
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
        Serializer.courseToMarkdown(course)
        println("> Done.")
    }

    def testMultipleCourseToMarkdown() = {
        val codes = ParVector("12M040", "11X001", "13M016A", "14M252", "12X050", "14P017")
        codes.foreach(code => {
            println(f"Building course $code")
            val course = Course(code)
            println(f"converting $code to markdown")
            Serializer.courseToMarkdown(course)
            println(f"> $code done.\n-------\n")
        })
    }

    /* def testSpdfLib() = {
        // Create a new Pdf converter with a custom configuration
        // run `wkhtmltopdf --extended-help` for a full list of options
        val pdf = spdf.Pdf(new PdfConfig {
            orientation := spdf.Portrait
            pageSize := "Letter"
            marginTop := "2"
            marginBottom := "0"
            marginLeft := "3"
            marginRight := "0"
        })
        // Save the PDF of Google's homepage into a file
        val path = Path.of("res/templates/desc-2022-11X001.html")
        val cntn = Files.readString(path)
        pdf.run(cntn, new File("ipa.pdf"))
    } */

    def main(args: Array[String]): Unit = {
        println("\n\n")
        // writeCoursDecsToRes("14M252")
        // testJsonLib()
        // testResolveCoursHours()
        // testCourseFactoryMethod()
        // testCourseToMarkdown()
        testMultipleCourseToMarkdown()

        println("\n\n")
    }
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
