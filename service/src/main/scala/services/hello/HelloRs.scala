package services.hello


import cats.effect.IO
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

object HelloRs {
  val HELLO = "hello"

  val service = HttpService[IO] {
    case GET -> Root / HELLO / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }
}
