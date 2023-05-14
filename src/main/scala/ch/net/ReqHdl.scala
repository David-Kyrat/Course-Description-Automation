package ch.net

import ch.Helpers._
import scala.io.Source
import scala.util.{Failure, Success, Using}
import java.io.IOException
import com.google.gson.JsonObject
import scala.collection.parallel.immutable.ParVector
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ParIterable

/**
 * Class representing an HTTP request, methods in the object `ReqHdl` returns
 * instance of this class to be able to do other useful things with a request
 * before actually "executing" it.
 *
 * @param request String, url of request
 * @param page index of page of result (optional, defaults to 0)
 */
case class ReqHdl private[net] (val req: String, val page: Int = 0) extends Function0[Resp] {

    /**
     * Execute the `GET` request
     * @return Server Response wrapped in an instance of the case class Resp
     *
     * If request failed: returned `Resp` will contain the error message and will return `true` on `isError()`
     */
    override def apply(): Resp = {
        try {
            Resp(ReqHdl.request(req), page)
        } catch {
            case e: Exception => Resp("", page, Some(e.getMessage()))
        }
    }

    /**
     * Execute the `GET` request and directly formats the json instead of
     * creating a `Resp` instance.
     * @return Server JSON Response
     *
     * If request failed:
     * @throws IllegalArgumentException
     */
    @throws(classOf[IllegalArgumentException])
    private def get(): String = ReqHdl.request(req)

    override def toString(): String = this.req

}

/**
 * HTTP Request Handler
 */
object ReqHdl {
    import Resp.toJsonObject

    /** API entry point */
    val baseUrl: String = "https://pgc.unige.ch/main/api"

    private lazy val spPart = "study-plans"
    private lazy val spNodePart = "study-plan-nodes"
    private lazy val coursePart = "teachings"

    /** solely study plan contains almost no info. See `studyPlanNodeUrl` to get the contained course etc... */
    // lazy val studyPlanUrl: String = f"$baseUrl/$spPart"

    /** will return study plan info and the courses inside it*/
    lazy val studyPlanNodeUrl: String = f"$baseUrl/$spNodePart"
    lazy val courseUrl = f"$baseUrl/$coursePart" // append courseYear-courseId

    // private def sp(size: Int, page: Int) = f"$studyPlanUrl?size=$size&page=$page"
    private def sp(size: Int, page: Int) = f"$baseUrl/$spPart?size=$size&page=$page"

    /**
     * Simple http GET request for given url
     * @param url request to perform
     * @return server's response
     *
     * If request failed:
     * @throws IllegalArgumentException
     */
    @throws(classOf[IllegalArgumentException])
    private def request(url: String): String =
        Using(Source.fromURL(url))(_.mkString) match {
            case Success(response: String) => response
            case Failure(reason) =>
                throw new IllegalArgumentException(f"`ReqHDL.urluest()`: HTTP Request Failed, reason: $reason")

        }

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
     * @param size Int, number of results (optional, defaults to 2000) NB: MAXIMUM (accepted by api) IS 2000
     *
     * @return new Request i.e. `ReqHdl` instance, requesting a list of study-plans if id was not given and details about study-plan with given `id` if it was
     */
    def studyPlan(id: String = null, size: Int = 2000): ReqHdl =
        if (id == null) g(f"$spPart?size=$size") else g(f"$spNodePart/$id?size=$size")

    /**
     * Same as `studyPlan()` but for couse, see [[ch.ReqHdl.studyPlan]] for more infos
     *
     * @param id
     * @param size Int, number of results (optional, defaults to 1000)
     * @return new Request i.e. `ReqHdl` instance, requesting a list of courses if id was not given and details about course with given `id` if it was
     */
    def course(id: String = null, size: Int = 1000): ReqHdl =
        if (id == null) g(f"$coursePart/find?size=$size") else g(f"$coursePart/$id")

    // BUG: Request 'https://pgc.unige.ch/main/api/teachings/find' does not work (error 400)

    /**
     * Follows each `next` link, extracting the underlying `JsonObject` until there is no more result to get
     *
     * NOTE: This method needs only to be called when retrieving study plans
     *
     * @param size amount of element to get for each parallel request
     * @return Vector of each response's page
     */
    def AllStudyPlan(size: Int = 1500): ParVector[JsonObject] = {
        val r1 = studyPlan(size = size).apply().jsonObj
        val totPage = r1.getAsJsObj("_page").getAsInt("totalPages")
        val reqAmount = totPage

            /* val responses = (0 until reqAmount)
                .toVector
                .view
                .par
                .flatMap(pageNb => toJsonObject(request(sp(size, pageNb))).getAsScalaJsObjIter("_data"))
            responses */

        (0 until reqAmount)
            .toVector
            .par
            .flatMap(pageNb => 
                    toJsonObject(request(sp(size, pageNb)))
                    .getAsScalaJsObjIter("_data")
                    )
    }
}
