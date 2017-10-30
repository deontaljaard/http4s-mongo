package server

import cats.implicits._
import core.person.AsyncPersonRegistry
import fs2.Task
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import services.PersonRs
import services.auth.AuthRs
import services.hello.HelloRs

import scala.util.Properties.envOrNone

object Http4sServer extends StreamApp {

  val port: Int = envOrNone("HTTP_PORT").map(_.toInt).getOrElse(8080)

  val services: HttpService = HelloRs.service |+|
    PersonRs(AsyncPersonRegistry).personRsService |+|
    AuthRs.service

  override def stream(args: List[String]): fs2.Stream[Task, Nothing] = {
    BlazeBuilder
      .bindHttp(port, "localhost")
      .mountService(services, "/api")
      .serve
  }

}
