package org.seanpquig.mini.search

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.seanpquig.mini.search.core.{JsonSupport, SearchResponse}

object Routes extends JsonSupport {

  def all: Route = {
    pathSingleSlash {
      complete("Mini Search")
    } ~ searchRoutes ~ indexingRoutes
  }

  def searchRoutes: Route = {
    path("search" / Segment) { idxName =>
      complete(SearchResponse(docs = List()))
    }
  }

  def indexingRoutes: Route = {
    path("index" / Segment) { idxName =>
      get {
        complete(s"Index info for $idxName")
      } ~
      post {
        complete(s"Create index: $idxName")
      }
    }
  }

}
