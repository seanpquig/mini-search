package org.seanpquig.mini.search

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Routes {

  def all: Route = {
    pathSingleSlash {
      complete("Mini Search")
    } ~ searchRoutes ~ indexingRoutes
  }

  def searchRoutes: Route = {
    path("search" / Segment) { idxName =>
      complete(s"Searching index: $idxName")
    }
  }

  def indexingRoutes: Route = {
    path("index" / Segment) { name =>
      get {
        complete(s"Index info for $name")
      } ~
      post {
        complete(s"Create index: $name")
      }
    }
  }

}
