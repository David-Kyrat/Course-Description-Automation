object Main extends App {
  println("\n\n")
  
  val rh = new ReqHdl();
  val descIpa22 = "teachings/2022-11X001"
  val serverResponse = rh(descIpa22)

  println(serverResponse)

  println("\n\n")
}
