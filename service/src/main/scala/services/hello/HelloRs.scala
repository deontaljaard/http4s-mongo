package services.hello

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object HelloRs {
  val HELLO = "hello"

  type OptionTIO[A] = OptionT[IO, A]

  val helloRsService: Kleisli[OptionTIO, Request[IO], Response[IO]] = HttpService[IO] {
    case GET -> Root / HELLO / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }
}
