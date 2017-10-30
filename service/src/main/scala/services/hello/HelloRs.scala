package services.hello

import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object HelloRs {
  val HELLO: String = "hello"

  val service = HttpService {
    case GET -> Root / HELLO / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }
}
