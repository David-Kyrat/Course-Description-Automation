import scala.io.Source

/**
  * HTTP Request Handler
  */
class ReqHdl(val baseUrl: String = "https://pgc.unige.ch/main/api") extends Function1[String, String] {

  /**
    * Simple `GET` request for given request to API defined in `this.baseUrl`
    * @param endpoint end of url to be appended to `this.baseUrl`
    * @return server raw json response 
    */
  override def apply(endpoint: String): String = Source.fromURL(f"$baseUrl/$endpoint").mkString

}


