package ch

import ch.Resp._
import ch.ReqHdl._

object Main extends App {
  println("\n\n")

  val descIpa22 = "teachings/2022-11X001"
  val rh: ReqHdl = ReqHdl.g(descIpa22)
  val serverResponse: Resp = rh()
   
  println(serverResponse)

  val studyPlans = gStudyPlan()
  // println(studyPlans)
  // println(serverResponse)

  println("\n\n")
}
