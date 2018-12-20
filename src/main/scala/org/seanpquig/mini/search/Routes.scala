package org.seanpquig.mini.search

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import org.nd4j.linalg.factory.Nd4j
import org.seanpquig.mini.search.core._
import org.seanpquig.mini.search.ml.ImageVisionTagger

object Routes extends JsonSupport {

  val logger = Logger("mini-search-routes")

  def all: Route = {
    pathSingleSlash {
      complete("Mini Search")
    } ~ searchRoutes ~ indexingRoutes
  }

  def searchRoutes: Route = ignoreTrailingSlash {
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

  private val imgTagger = new ImageVisionTagger(Config.modelPath)

  def indexingRoutes: Route = ignoreTrailingSlash {
    path("index" / Segment) { idxName =>
      get {
        complete(s"Index info for $idxName")
      } ~
      post {
        entity(as[IndexRequest]) { request =>
          complete(s"Create index: $idxName")
        }
      }
    } ~ path("indices") {
      complete(MiniSearch.indicesResponse())
    } ~ path("vision_upload") {
      val testArray = Nd4j.zeros(1, 3, 299, 299)
      complete(imgTagger.predictTags(testArray))
    }
  }

}
