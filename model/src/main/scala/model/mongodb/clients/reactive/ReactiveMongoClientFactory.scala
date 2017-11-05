package model.mongodb.clients.reactive

import common.AppConfig
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.core.errors.ConnectionException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ReactiveMongoClientFactory {
  val driver = MongoDriver()

  val connection: MongoConnection = driver.connection(List(AppConfig.DB_URL))

  private def getDatabase(databaseName: String): Future[DefaultDB] = connection.database(databaseName)

  def collectionFromDataBase(databaseName: String, collectionName: String): Future[BSONCollection] = {
    getDatabase(databaseName)
      .map(_.collection(collectionName))
      .recover {
        case e: Throwable =>
          throw ConnectionException(
            s"Connection cannot be established. Reason: ${e.getMessage}")
      }
  }
}
