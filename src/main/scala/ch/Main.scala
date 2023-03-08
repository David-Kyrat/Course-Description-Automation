package ch

// import ch.Resp._
import ch.ReqHdl._
import ch.Utils

import com.google.gson.{JsonElement, JsonParser, JsonSerializer, JsonDeserializer}
import java.nio.file.Path


object Main {
    def testGetStudyPlans() = {
        /*val bs = Using(Source.fromURL(f"${ReqHdl.baseUrl}/$descIpa22"))
        val resp = bs.mkString
        bs.close()
        resp
        println()

        val rh: ReqHdl = ReqHdl.g(descIpa22)
        val serverResponse: Resp = rh()
        println(serverResponse) */

        // println("\n\n----------------------- Study Plans -----------------------------")
        val studyPlansReq: ReqHdl = studyPlan()
        val studyPlansResp: Resp = studyPlansReq()
        println(studyPlansResp)
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


    def main(args: Array[String]): Unit = {
        print("\u001b[2J") // clears sbt output TODO: remove for Production!

        val jsonString = Utils.prettifyJson(course("2022-11X001").get())
        Utils.write(Path.of("res/ipa-22-desc.json"), jsonString)


    }
}
