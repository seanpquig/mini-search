package org.seanpquig.mini.search

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger

object Service {

  val interface = "::0"
  val port = 8080
  val logger = Logger("mini-search-service")

  implicit val system = ActorSystem("mini-search-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(Routes.all, interface, port)
    logger.info(s"MiniSearch Server running at http://localhost:$port")
  }

}
