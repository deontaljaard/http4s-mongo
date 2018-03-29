package services

import cats.effect.IO
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import model.mongodb.clients.async.AsyncMongoOps._

object Encoders {
  implicit def booleanEncoder: EntityEncoder[IO, Boolean] = jsonEncoderOf[IO, Boolean]
}
