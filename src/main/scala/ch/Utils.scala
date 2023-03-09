package ch

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import spray.json._

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path}
import java.nio.file.StandardOpenOption._
import scala.language.postfixOps

import io.{Source, BufferedSource}
import DefaultJsonProtocol._

object Utils {
    private val gson: Gson = new GsonBuilder().setPrettyPrinting().create()

    def read(path: Path) = String.join("\n", Files.readAllLines(path, UTF_8))
    def write(path: Path, content: String) = Files.writeString(path, content, UTF_8, CREATE_NEW, APPEND)

    /**
     * @param rawJson String
     * @return prettify json string, i.e. indented ...
     */
    def prettifyJson(rawJson: String) = rawJson.parseJson.prettyPrint
}
