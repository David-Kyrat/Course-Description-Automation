package ch

import com.google.gson.{Gson, GsonBuilder}
import spray.json._

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption._

import io.{Source, BufferedSource}
import DefaultJsonProtocol._
import com.google.gson.reflect.TypeToken

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
