package ch


import ch.ReqHdl._

object Main extends App {
  println("\n\n")
  
  val descIpa22 = "teachings/2022-11X001"
  //val serverResponse = rh(descIpa22)
  val studyPlans = gStudyPlan()
  println(studyPlans)
  //println(serverResponse)

  println("\n\n")
}
