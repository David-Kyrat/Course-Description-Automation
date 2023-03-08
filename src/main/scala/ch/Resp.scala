package ch

import ch.Utils.prettifyJson

/** Cases class modelizing the server's response to request. Allows doing
  * several useful operations with it (i.e. ask for next page)
  */
final case class Resp(val resp: String, val page: Int = 0) {


  override def toString(): String = prettifyJson(this.resp)

}

object Resp {

  /** @param rawJson
    *   String
    * @return
    *   prettify json string, i.e. indented ...
    */
  // def prettifyJson(rawJson: String) = rawJson.parseJson.prettyPrint
}
