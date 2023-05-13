package ch

import scala.jdk.CollectionConverters._
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import scala.collection.parallel.immutable.ParVector

object Helpers {

    implicit class JsonArrayOps(el: JsonArray) {

        /**
         * Shorthand for `this.asScala.map(_.getAsJsonObject())`
         * @return JsonArray to convert to scala `Iterable[JsonObject]`
         */
        def getAsScalaJsObjIter(): Iterable[JsonObject] = el.asScala.map(_.getAsJsonObject)
    }

    implicit class JsonElementOps(el: JsonElement) {

        /**
         * @return the member with the specified name as a String
         * NB: `this` must be a `jsonObject`!
         */
        def getAsStr(memberName: String): String = el.getAsJsonObject().get(memberName).getAsString

        /**
         * @return the member with the specified name as an Int
         * NB: `this` must be a `jsonObject`!
         */
        def getAsInt(memberName: String): Int = el.getAsJsonObject().get(memberName).getAsInt

        /**
         * Shorthand for `el.getAsJsonArray.asScala.to(ParVector)`
         *
         * @param el `JsonElement` to convert to scala iterable
         * @return converted collection
         */
        def getAsParVec() = el.getAsJsonArray.asScala.to(ParVector)

        /**
         * Shorthand for `el.getAsJsonArray.asScala`
         *
         * @param el `JsonElement` to convert to scala iterable
         * @return iterable of JsonElement
         */
        def getAsIter(): Iterable[JsonElement] = el.getAsJsonArray.asScala

        /**
         * Shorthand for `el.getAsJsonArray.asScala.map(_.getAsJsonObject())`
         *
         * @param el `JsonElement` to convert to scala iterable
         * @return iterable of JsonObject
         */
        def getAsJsonObjIter(): Iterable[JsonObject] = el.getAsJsonArray.asScala.map(_.getAsJsonObject)

    }

    implicit class JsonObjOps(jo: JsonObject) {

        /** @return the member with the specified name as a String */
        def getAsStr(memberName: String): String = jo.get(memberName).getAsString

        /** @return the json object with the specified name as a `JsonObject` */
        def getAsJsObj(memberName: String): JsonObject = jo.get(memberName).getAsJsonObject

        /** @return the json array with the specified name as a `JsonArray` */
        def getAsJsArr(memberName: String): JsonArray = jo.get(memberName).getAsJsonArray

        /** @return the json array with the specified name as a `JsonArray` converted to a scala `Iterable` */
        def getAsScalaIter(memberName: String): Iterable[JsonElement] = jo.get(memberName).getAsJsonArray.asScala

        /**
         *  @return the array with the specified name as a `JsonArray` converted to a scala `Iterable` of `JsonObject`
         *  i.e. Shorthand for `this.get(memberName).getAsJsonArray.asScala.map(_.getAsJsonObject)`
         */
        def getAsScalaJsObjIter(memberName: String): Iterable[JsonObject] = jo.get(memberName).getAsJsonArray.asScala.map(_.getAsJsonObject)

        /** @return the member with the specified name as an `Int` */
        def getAsInt(memberName: String): Int = jo.get(memberName).getAsInt

        /** @return the member with the specified name as a `Boolean` */
        def getAsBool(memberName: String): Boolean = jo.get(memberName).getAsBoolean()

        /**
         * Shorthand to get nested member i.e. does `this.getAsJsObj(name1).getAsJsObj(name2)...`
         *
         * @param memberNames names of the nested member  WARN MUST BE MORE THAN 1!
         * @return Nested member e.g. `_page.currentPage`
         */
        /* def getNested(memberNames: String*): JsonElement = {
            val it = memberNames.iterator
            var crt = jo.getAsJsObj(it.next)
            while (it.hasNext) {
                crt =

            }
        } */
    }
}
