package org.seanpquig.mini.search

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger


object Service {

  val interface = "::0"
  val port = 8080
  val logger = Logger("mini-search-service")

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  def routes: Route = {
    pathSingleSlash {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Mini Search</h1>"))
    } ~
    path("search") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(routes, interface, port)
    logger.info(s"MiniSearch Server running at http://localhost:$port")
  }

}
