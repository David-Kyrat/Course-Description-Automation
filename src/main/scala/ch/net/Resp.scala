package ch.net

import ch.Helpers.{JsonObjOps, JsonElementOps}
import com.google.gson._
import scala.collection.mutable.ArrayBuffer

/**
 * Cases class modelizing the server's response to request. Allows doing
 * several useful operations with it (i.e. ask for next page).
 * NB: Can contain an error !
 *
 * @param resp raw Json response from server
 * @page index of page
 * @errMsg None if everything went well, error message otherwise
 */
final case class Resp(val resp: String, val page: Int = 0, val errMsg: Option[String] = None) {
    import Resp.gson

    override def toString: String = jsonObj.toString()

    // search for next page link {"next": ""} ... in result
    def hasNext: Boolean = {
        val links = jsonObj.getAsJsObj("_links")
        try {
            val pageMetaData = jsonObj.getAsJsObj("_page")
            val crtPage = pageMetaData.getAsInt("currentPage")
            val totPage = pageMetaData.getAsInt("totalPages")
            println("lastPage: " + totPage)
            val url = links.getAsJsObj("next").getAsStr("href")
            // println(f"crtPage: $crtPage < $totPage : totPage\n--------------\n")
            return crtPage < totPage - 1
        } catch {
            case e: Exception => {
                return false
            }
        }
        true
    }

    /**
     * Directly convert the server's response from json string
     * to a `JsonObject` (api `google.Gson`)
     * @return `JsonObject` that can be traversed kind of like a Map with a `get()` method
     * (no need to manually parse it)
     *
     * If `this.isError`:
     * @throws IllegalArgumentException
     */
    @throws(classOf[IllegalArgumentException])
    def jsonObj: JsonObject = {
        if (isError) throw new IllegalStateException(String.format("This Response is an error, cannot get JsonObject.\n Reason: %s", errMsg.getOrElse("")))
        else gson.fromJson(resp, classOf[JsonObject])
    } // gson.fromJson(resp, classOf[JsonObject])

    /**
     * @param _nb Page number (if optional defaults to `this.page + 1`)
     * @return Next page for current request or None if page didn't have a next field / request failed
     */
    def next(_nb: Int = 0): Option[Resp] = {
        val links = jsonObj.getAsJsObj("_links")
        try {
            val url = links.getAsJsObj("next").getAsStr("href")
            val crtPage = if (page != 0) page else jsonObj.getAsJsObj("_page").getAsInt("currentPage")
            val nb = if (_nb <= 0) page + 1 else _nb
            val resp = ReqHdl(url, nb)()
            Some(resp)
        } catch {
            case e: Exception => None
        }
    }

    /**
     * Follows each `next` link, extracting the underlying `JsonObject` until there is no more result to get
     *
     * NOTE: This method needs only to be called when retrieving study plans
     *
     * @return Vector of each response's page
     */
    def nextAll: Vector[JsonObject] = {
        var buffer: ArrayBuffer[JsonObject] = new ArrayBuffer()
        var crt: Resp = this
        while (crt.hasNext) {
            buffer += crt.jsonObj
            crt = {
                val nextUrl = crt.jsonObj.getAsJsObj("_links").getAsJsObj("next").getAsStr("href")
                ReqHdl(nextUrl).apply()
            }
        }
        buffer.toVector
    }

    def isError: Boolean = !errMsg.isEmpty

    /** @return Prettified json response */
    def prettify: String = Resp.prettify(jsonObj)
}

object Resp {
    val gson = new GsonBuilder().setPrettyPrinting().create()

    /** @return Prettified json  string */
    def prettify(je: JsonElement): String = gson.toJson(je)
}
