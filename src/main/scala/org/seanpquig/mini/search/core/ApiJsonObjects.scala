package org.seanpquig.mini.search.core

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * This file contains classes that support the different interfaces of the MiniSearch API.
  * Including requests and responses and support proper JSON (un)marshalling.
  */

//JSON support that can be utilized in routes
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val searchRequestFormat: RootJsonFormat[SearchRequest] = jsonFormat2(SearchRequest)
  implicit val searchResponseFormat: RootJsonFormat[SearchResponse] = jsonFormat1(SearchResponse)
  implicit val documentFormat: RootJsonFormat[Document] = jsonFormat3(Document)
}

case class SearchRequest(queryTerm: String, limit: Int)

case class SearchResponse(docs: Seq[Document])
