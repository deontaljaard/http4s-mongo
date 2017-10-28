package server

import java.util.concurrent.{ExecutorService, Executors}

import cats.implicits._
import fs2.Task
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import services.{AuthRs, HelloRs, PersonRs}

import scala.util.Properties.envOrNone

object ScalaMongoServer extends StreamApp {

  val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
  val ip   : String           = "0.0.0.0"
  val pool : ExecutorService  = Executors.newCachedThreadPool()

  val services: HttpService = HelloRs.service |+| PersonRs.service |+| AuthRs.service

  override def stream(args: List[String]): fs2.Stream[Task, Nothing] = {
    BlazeBuilder
    .bindHttp(8080, "localhost")
    .mountService(services, "/api")
    .serve
  }
}
