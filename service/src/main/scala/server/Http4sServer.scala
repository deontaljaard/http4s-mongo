package server

import cats.data.{Kleisli, OptionT}
import cats.effect._
import core.file.AwsS3FileRegistry
import core.person.AsyncPersonRegistry
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s._
import org.http4s.server.blaze.BlazeBuilder
import services.PersonRs
import services.auth.AuthRs
import services.file.FileRs
import services.hello.HelloRs
import cats._
import cats.data._
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties.envOrNone

object Http4sServer extends StreamApp[IO] {

  val port: Int = envOrNone("HTTP_PORT").map(_.toInt).getOrElse(8080)

  type OptionTIO[A] = OptionT[IO, A]

  val services: Kleisli[OptionTIO, Request[IO], Response[IO]] = HelloRs.helloRsService/* <+>
    PersonRs(AsyncPersonRegistry).personRsService <+>
    FileRs(AwsS3FileRegistry).fileRsService <+>*/
//    AuthRs.service

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .mountService(services, "/api")
      .serve


}
