package ch


/**
  * Cases class modelizing the server's response to request. Allows doing several useful operations with it (i.e. ask for next page)
  */
final case class Resp(val req: String, val page: Int = 0) {

  /**
    * @param page (optional) index of page to request.
    * If not given, defaults to `this.page + 1` 
    * @return next page of result for the request `this.req`
    */
  def next(page: Int = 0): Resp = ???

  override def toString(): String = req
}
