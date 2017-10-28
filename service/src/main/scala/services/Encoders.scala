package services

import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import model.mongodb.clients.async.AsyncMongoOps._

object Encoders {
  implicit val booleanEncoder: EntityEncoder[Boolean] = jsonEncoderOf[Boolean]
}
