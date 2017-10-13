package model.mongodb

import io.circe.{Decoder, Encoder, HCursor, Json}
import org.mongodb.scala.bson.ObjectId

object MongoOps {
  implicit val encodeObjectId: Encoder[ObjectId] = (id: ObjectId) => Json.fromString(id.toString)

  implicit val decodeObjectId: Decoder[ObjectId] = (c: HCursor) => for {
    id <- c.field("_id").as[String]
  } yield {
    new ObjectId(id)
  }
}
