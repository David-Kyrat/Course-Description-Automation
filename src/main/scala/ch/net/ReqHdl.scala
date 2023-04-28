package ch.net

import scala.io.Source
import scala.util.{Failure, Success, Using}

/**
 * Class representing an HTTP request, methods in the object `ReqHdl` returns
 * instance of this class to be able to do other useful things with a request
 * before actually "executing" it.
 *
 * @param request String, url of request
 * @param page index of page of result (optional, defaults to 0)
 */
case class ReqHdl private (val req: String, val page: Int = 0) extends Function0[Resp] {

    /**
     * Execute the `GET` request
     * @return Server Response wrapped in an instance of the case class Resp
     */
    override def apply(): Resp = Resp(request(req), page)

    /**
     * Simple http GET request for given url
     * @param url string
     * @return server's response
     * @throws IOException if request failed
     */
    private def request(url: String): String =
        Using(Source.fromURL(req))(_.mkString) match {
            case Success(response: String) => response
            case Failure(reason) =>
                throw new IllegalArgumentException(f"`ReqHDL.request()`: HTTP Request Failed, reason: $reason")
        }

    /**
     * Execute the `GET` request and directly formats the json instead of
     * creating a `Resp` instance.
     * @return Server JSON Response
     */
    def get(): String = request(req)

    /**
     * Return next page of current request. (faster than looking through the
     * significatively long & complex json response for the "next" item)
     * @param page (optional) index of page to request. If not given, defaults to `this.page + 1`
     * @return Request for the next page of result of current request
     */
    def next(newPage: Int = 0): ReqHdl = {
        var resolvedNewPage = if (newPage == 0) this.page + 1 else newPage
        val newReq =
            if (this.page == 0) {
                val tokenToAdd = if (this.req.contains('?')) '&' else '?' // if request already contains the '?' for advanced search => do not re-add it and add a '&' instead

                f"${this.req}${tokenToAdd}page=$resolvedNewPage"

            } else {
                val patternPair = ("page=", 5) // pair ["pattern", "patternLength"]
                val idx = this.req.lastIndexOf(patternPair._1) + patternPair._2 // idx of pageNb
                this.req.substring(0, idx + 1) + f"$resolvedNewPage"
            }
        // if were in else: then url is of the form "$baseUrl/...&page=x" where x is this.page => hence replacing it
        /* TODO: Check if there are url for page != 0, that do not end in "page=XX"
         * if there aren't => we can just replace the everything from "page=..." to the end and append the new pageNb */

        ReqHdl(newReq, newPage)
    }

    def nextAll(): Vector[Resp] = ???
    /* NOTE: Only things that indicates that there are no more results is the absence
     * of "next" item in the json response => so we do have to:
     * 1. Search for it in the end
     * 2. And actually execute those request to look for the presence of that `next` item
     */

    override def toString(): String = this.req

}

/**
 * HTTP Request Handler
 */
object ReqHdl {

    /** API entry point */
    val baseUrl: String = "https://pgc.unige.ch/main/api"

    private val spPart = "study-plans"
    private val coursePart = "teachings"

    val studyPlanUrl: String = f"$baseUrl/$spPart"
    val courseUrl = f"$baseUrl/$coursePart" // append courseYear-courseId

    /**
     * Instantiate ReqHdl class with a new request, to execute it call the apply
     * method i.e. `ReqHdl(req)()` or `ReqHdl(req).apply()`
     * @param endpoint end of url to be appended to `this.baseUrl`
     *
     * @return `ReqHdl` instance
     */
    def g(endpoint: String) = ReqHdl(f"$baseUrl/$endpoint")

    /**
     * @param id String, exact url-id of the form `studyPlanYear-studyPlanUrlId`. (Optional) if not given, defaults to aksing for the list of studyPlans
     * @param size Int, number of results (optional, defaults to 1000)
     * @return new Request i.e. `ReqHdl` instance, requesting a list of study-plans if id was not given and details about study-plan with given `id` if it was
     */
    def studyPlan(id: String = null, size: Int = 1000) =
        if (id == null) g(f"$spPart?size=$size") else g(f"$spPart/$id?size=$size")

    /**
     * Same as `studyPlan()` but for couse, see [[ch.ReqHdl.studyPlan]] for more infos
     *
     * @param id
     * @param size Int, number of results (optional, defaults to 1000)
     * @return new Request i.e. `ReqHdl` instance, requesting a list of courses if id was not given and details about course with given `id` if it was
     */
    def course(id: String = null, size: Int = 1000) =
        if (id == null) g(f"$coursePart/find?size=$size") else g(f"$coursePart/$id")

    // BUG: Request 'https://pgc.unige.ch/main/api/teachings/find' does not work (error 400)

}
