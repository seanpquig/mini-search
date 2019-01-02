package org.seanpquig.mini.search.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.seanpquig.mini.search.ml.ImagenetPrediction
import spray.json._

/**
  * This file contains classes that support the different interfaces of the MiniSearch API.
  * Including requests and responses and support proper JSON (un)marshalling.
  */

//JSON support that can be utilized in routes
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val searchRequestFormat: RootJsonFormat[SearchRequest] = jsonFormat2(SearchRequest)

  implicit object TextDocJsonFormat extends RootJsonFormat[TextDoc] {
    def write(d: TextDoc) = JsObject(
      "id" -> JsString(d.id),
      "text" -> JsString(d.text),
      "title" -> JsString(d.title.getOrElse(""))
    )

    def read(value: JsValue): TextDoc = {
      value.asJsObject.getFields("text", "title") match {
        case Seq(JsString(text), JsString(title)) => TextDoc(text = text, title = Option(title))
        case _ => deserializationError("Document object expected")
      }
    }
  }

  implicit val searchResponseFormat: RootJsonFormat[SearchResponse] = jsonFormat2(SearchResponse)
  implicit val indexRequestFormat: RootJsonFormat[IndexRequest] = jsonFormat1(IndexRequest)
  implicit val indexInfoFormat: RootJsonFormat[IndexInfo] = jsonFormat2(IndexInfo)
  implicit val indicesResponseFormat: RootJsonFormat[IndicesResponse] = jsonFormat1(IndicesResponse)
  implicit val imagenetPredFormat: RootJsonFormat[ImagenetPrediction] = jsonFormat3(ImagenetPrediction)
}

case class SearchRequest(query: String, limit: Int)

case class SearchResponse(message: String, docs: Iterable[TextDoc])

case class IndexRequest(docs: Iterable[TextDoc])

case class IndexResponse(message: String)

case class IndexInfo(name: String, docCount: Long)
case class IndicesResponse(indices: Iterable[IndexInfo])
