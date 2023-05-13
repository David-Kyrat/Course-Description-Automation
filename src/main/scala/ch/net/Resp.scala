package ch.net

import com.google.gson._

/**
 * Cases class modelizing the server's response to request. Allows doing
 * several useful operations with it (i.e. ask for next page)
 */
final case class Resp(val resp: String, val page: Int = 0) {
    import Resp.gson

    override def toString: String = jsonObj.toString()

    // search for next page link {"next": ""} ... in result
    def hasNext: Boolean = ???

    /**
     * Directly convert the server's response from json string
     * to a `JsonObject` (api `google.Gson`)
     * @return `JsonObject` that can be traversed kind of like a Map with a `get()` method
     * (no need to manually parse it)
     */
    def jsonObj: JsonObject = gson.fromJson(resp, classOf[JsonObject])
}

object Resp {
    val gson = new GsonBuilder().setPrettyPrinting().create()

}
