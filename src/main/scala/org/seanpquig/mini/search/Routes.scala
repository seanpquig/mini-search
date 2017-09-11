package org.seanpquig.mini.search

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import org.seanpquig.mini.search.core.{IndexRequest, JsonSupport, SearchRequest, SearchResponse}

object Routes extends JsonSupport {

  val logger = Logger("mini-search-routes")

  def all: Route = {
    pathSingleSlash {
      complete("Mini Search")
    } ~ searchRoutes ~ indexingRoutes
  }

  def searchRoutes: Route = {
    path("search" / Segment) { idxName =>
      post {
        entity(as[SearchRequest]) { request =>
          // Attempt to search against index
          val docsOpt = MiniSearch.search(idxName, request.query)
          val response = docsOpt.map(_.take(request.limit))
            .map(SearchResponse(s"Successful search against $idxName", _))
            .getOrElse(SearchResponse(s"Index $idxName does not exist.", List()))

          complete(response)
        }
      }
    }
  }

  def indexingRoutes: Route = {
    path("index" / Segment) { idxName =>
      get {
        complete(s"Index info for $idxName")
      } ~
      post {
        entity(as[IndexRequest]) { request =>
          complete(s"Create index: $idxName")
        }
      }
    }
  }

}
