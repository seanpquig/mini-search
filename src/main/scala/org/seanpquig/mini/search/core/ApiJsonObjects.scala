package org.seanpquig.mini.search.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

/**
  * This file contains classes that support the different interfaces of the MiniSearch API.
  * Including requests and responses and support proper JSON (un)marshalling.
  */

//JSON support that can be utilized in routes
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val searchRequestFormat: RootJsonFormat[SearchRequest] = jsonFormat2(SearchRequest)

  implicit object DocumentJsonFormat extends RootJsonFormat[Document] {
    def write(d: Document) = JsObject(
      "id" -> JsString(d.id),
      "text" -> JsString(d.text),
      "title" -> JsString(d.title.getOrElse(""))
    )

    def read(value: JsValue): Document = {
      value.asJsObject.getFields("text", "title") match {
        case Seq(JsString(text), JsString(title)) => Document(text = text, title = Option(title))
        case _ => deserializationError("Document object expected")
      }
    }
  }
  implicit val searchResponseFormat: RootJsonFormat[SearchResponse] = jsonFormat2(SearchResponse)
  implicit val indexRequestFormat: RootJsonFormat[IndexRequest] = jsonFormat1(IndexRequest)
}

case class SearchRequest(query: String, limit: Int)

case class SearchResponse(message: String, docs: Iterable[Document])

case class IndexRequest(docs: Iterable[Document])

case class IndexResponse(message: String)
