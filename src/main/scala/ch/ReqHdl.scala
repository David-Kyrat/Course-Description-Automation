package ch

import scala.io.Source

/** HTTP Request Handler
  */
object ReqHdl {

  /** API entry point */
  val baseUrl: String = "https://pgc.unige.ch/main/api"

  /** Simple `GET` request for given request to API defined in `this.baseUrl`
    * @param endpoint
    *   end of url to be appended to `this.baseUrl`
    * @return
    *   server raw json response
    */
  def g(endpoint: String) = Resp(Source.fromURL(f"$baseUrl/$endpoint").mkString)

  private val studyPlanUrl: String = f"$baseUrl/study-plans"

  def gStudyPlan(id : Int = 0) = if (id == 0) g(studyPlanUrl) else g(f"$studyPlanUrl/$id")
 
}
