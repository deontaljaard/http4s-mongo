package model.mongodb.clients

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.core.errors.ConnectionException

import scala.concurrent.Future

object ReactiveMongoClientFactory {
  val driver = MongoDriver()

  val connection: MongoConnection = driver.connection(List("localhost"))

  private def getDatabase(databaseName: String): Future[DefaultDB] = connection.database(databaseName)

  def collectionFromDataBase(databaseName: String, collectionName: String): Future[BSONCollection] = {
    getDatabase(databaseName)
      .map(_.collection(collectionName))
      .recover {
        case e: Throwable =>
          throw new ConnectionException(
            s"Connection cannot be established. Reason: ${e.getMessage}")
      }
  }
}
