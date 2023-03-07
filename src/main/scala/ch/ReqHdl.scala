package ch

import scala.io.Source

case class ReqHdl private(val request: String, val page: Int = 0) extends Function0[Resp] {

  /**
    * Execute the `GET` request
    *
    * @return Server Response wrapped in an instance of the case class Resp
    */
  override def apply(): Resp = Resp(Source.fromURL(request).mkString)

  
  /** @param page
    *   (optional) index of page to request. If not given, defaults to
    *   `this.page + 1`
    * @return
    *   next page of result for the request `this.req`
    */
  def next(page: Int = 0): Resp = ???

  override def toString(): String = this.request

}

/** HTTP Request Handler
  */
object ReqHdl {

  /** API entry point */
  val baseUrl: String = "https://pgc.unige.ch/main/api"

  /** Simple `GET` request for given request to API defined in `this.baseUrl`
   *
   * Instantiate ReqHdl class with a new request, to execute it call teh apply method i.e. `ReqHdl(req)()` or `ReqHdl(req).apply`
    * @param endpoint
    *   end of url to be appended to `this.baseUrl`
    * @return `ReqHdl` instance
    */
  def g(endpoint: String) = ReqHdl(f"$baseUrl/$endpoint")

  val studyPlanUrl: String = f"$baseUrl/study-plans"

  def gStudyPlan(id : Int = 0) = if (id == 0) g(studyPlanUrl) else g(f"$studyPlanUrl/$id")

}
