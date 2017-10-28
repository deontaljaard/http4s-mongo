package services

import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object HelloRs {

  val service = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }
}
