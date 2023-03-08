package ch

import scala.io.{Source, BufferedSource}
import scala.io.Codec.UTF8
import ch.Resp._

import scala.util.{Using, Success, Failure}

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
     *
     * @return prettified Server JSON Response
     */
    def get(): String = prettifyJson(request(req))

    /**
     * Return next page of current request. (faster than looking through the
     * significatively long & complex json response for the "next" item)
     * @param page (optional) index of page to request. If not given, defaults to `this.page + 1`
     * @return next page of result for the request `this.req`
     */
    def next(newPage: Int = 0): ReqHdl = {
        var resolvedNewPage = if (newPage == 0) this.page + 1 else newPage
        val newReq =
            if (this.page == 0) f"${this.req}?page=$resolvedNewPage"
            else {
                val patternPair = ("page=", 5) // pair ["pattern", "patternLength"]
                val idx = this.req.lastIndexOf(patternPair._1) + patternPair._2 // idx of pageNb
                this.req.substring(0, idx + 1) + f"$resolvedNewPage"
            }
        // if were in else: then url is of the form "$baseUrl/...&page=x" where x is this.page => hence replacing it
        /* TODO: Check if there are url for page != 0, that do not end in "page=XX"
         * if there aren't => we can just replace the everything from "page=..." to the end and append the new pageNb */

        ReqHdl(newReq, newPage)
    }

    override def toString(): String = this.req

}

/**
 * HTTP Request Handler
 */
object ReqHdl {

    /** API entry point */
    val baseUrl: String = "https://pgc.unige.ch/main/api"

    /**
     * Instantiate ReqHdl class with a new request, to execute it call the apply
     * method i.e. `ReqHdl(req)()` or `ReqHdl(req).apply()`
     * @param endpoint end of url to be appended to `this.baseUrl`
     *
     * @return `ReqHdl` instance
     */
    def g(endpoint: String) = ReqHdl(f"$baseUrl/$endpoint")

    val studyPlanUrl: String = f"$baseUrl/study-plans"

    /**
     * @param id String, exact url-id of the form `studyPlanUrlId-studyPlanYear`. (Optional) if not given, defaults to aksing for the list of studyPlans
     *
     * @return new Request i.e. `ReqHdl` instance, requesting a list of study-plans if id was not given and details about study-plan with given `id` if it was
     */
    def studyPlan(id: String = null) =
        if (id == null) g(studyPlanUrl) else g(f"$studyPlanUrl/$id")

}
