package ch

// import ch.Resp._
import ch.ReqHdl._
import ch.Utils
import java.nio.file.Path

object Main extends App {
    print("\u001b[2J") //clears sbt output TODO: remove for Production!

    val ipa22Id = "2022-11X001"
    val ipa22Req = ReqHdl.course(f"$ipa22Id?size=1000")
    val ipa22Resp = ipa22Req.get()
    Utils.write(Path.of("res/out.json"), ipa22Resp)

    // println(ipa22Req.get())
    //println(ipa22Req.next().get())
    // println(ipa22Req.next(26).get())


//  val bs = Using(Source.fromURL(f"${ReqHdl.baseUrl}/$descIpa22"))
    // val resp = bs.mkString
    // bs.close()
    // resp
    println()

    /* val rh: ReqHdl = ReqHdl.g(descIpa22)
  val serverResponse: Resp = rh()
  println(serverResponse)*/

    /* println("\n\n----------------------- Study Plans -----------------------------")
  val studyPlansReq: ReqHdl = studyPlan()
  val studyPlansResp: Resp = studyPlansReq()
  println(studyPlansResp) */
}
