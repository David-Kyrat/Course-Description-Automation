package ch

import java.nio.file.Path
import io.{Source, BufferedSource}
import java.nio.file.Files
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption._

import spray.json._
import DefaultJsonProtocol._


object Utils {

  def read(path: Path) = String.join("\n", Files.readAllLines(path, UTF_8))
  def write(path: Path, content: String) = Files.writeString(path, content, UTF_8, CREATE_NEW, APPEND)

  /** @param rawJson
    *   String
    * @return
    *   prettify json string, i.e. indented ...
    */
   def prettifyJson(rawJson: String) = rawJson.parseJson.prettyPrint

}

/* def read(path: Path) = {
  val bs = new BufferedSource(Source.fromFile(path.toFile))
  val cnt = bs.getLines().mkString
  bs.close()
  cnt
} */
