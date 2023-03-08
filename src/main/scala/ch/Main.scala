package ch

import ch.Resp._
import ch.ReqHdl._

import scala.io.{Source, BufferedSource}
import scala.io.Codec.UTF8
import scala.util.Using
import scala.util.Success
import scala.util.Failure


object Main extends App {
    println("\n\n")

    val descIpa22 = "teachings/2022-11X001"

    val x: String = Using(Source.fromURL(f"$baseUrl/$descIpa22"))(_.mkString) match {
        case Success(response: String) => response
        case Failure(reason)           => throw new IllegalArgumentException(f"`ReqHDL.request()`: HTTP Request Failed, reason: $reason")
    }
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

    println("\n\n")
}
