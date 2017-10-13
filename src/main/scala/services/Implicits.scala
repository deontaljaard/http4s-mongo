package services

import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import model.mongodb.MongoOps._

object Implicits {
  implicit val booleanEncoder: EntityEncoder[Boolean] = jsonEncoderOf[Boolean]
}
