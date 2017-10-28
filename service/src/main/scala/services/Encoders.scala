package services

import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import model.mongodb.clients.async.AsyncMongoOps._

object Encoders {
  implicit def booleanEncoder: EntityEncoder[Boolean] = jsonEncoderOf[Boolean]
}
