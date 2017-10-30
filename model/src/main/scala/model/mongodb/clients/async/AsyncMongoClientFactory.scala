package model.mongodb.clients.async

import common.AppConfig
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoDatabase, ServerAddress}

import scala.collection.JavaConverters._

object AsyncMongoClientFactory {

  //TODO: read connection URL from environment variable
  val clusterSettings: ClusterSettings = ClusterSettings.builder().hosts(List(new ServerAddress(AppConfig.DB_URL)).asJava).build()
  val settings: MongoClientSettings = MongoClientSettings.builder().clusterSettings(clusterSettings).build()
  val mongoClient: MongoClient = MongoClient(settings)

  def getDatabase(database: String, codecProvider: CodecProvider): MongoDatabase = {
    val codecRegistry = fromRegistries(fromProviders(codecProvider), DEFAULT_CODEC_REGISTRY)
    mongoClient.getDatabase(database).withCodecRegistry(codecRegistry)
  }

}

