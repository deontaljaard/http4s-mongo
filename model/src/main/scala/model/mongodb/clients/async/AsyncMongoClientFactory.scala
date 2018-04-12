package model.mongodb.clients.async

import com.mongodb.ConnectionString
import common.AppConfig
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.connection.{ClusterSettings, NettyStreamFactoryFactory, SslSettings}
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoDatabase}

import scala.util.Properties

object AsyncMongoClientFactory {

  private val dbUrl: String = Properties.envOrElse("DB_URL", AppConfig.DB_URL_LOCAL)

  private def initMongoClientSettings = {
    MongoClientSettings.builder().clusterSettings(clusterSettings)
  }

  private def buildMongoClientSettings = {
    initMongoClientSettings.build()
  }

  private def buildMongoClientSettingsWithSSL = {
    initMongoClientSettings
      .sslSettings(SslSettings.builder().enabled(true).build())
      .streamFactoryFactory(NettyStreamFactoryFactory()).build()
  }

  private val clusterSettings: ClusterSettings = ClusterSettings.builder()
    .applyConnectionString(new ConnectionString(dbUrl))
    .build()
  private val settings: MongoClientSettings = if (dbUrl.contains("localhost")) buildMongoClientSettings else buildMongoClientSettingsWithSSL
  private val mongoClient: MongoClient = MongoClient(settings)

  def getDatabase(database: String, codecProviders: Seq[CodecProvider]): MongoDatabase = {
    val codecRegistry = fromRegistries(fromProviders(codecProviders: _*), DEFAULT_CODEC_REGISTRY)
    mongoClient.getDatabase(database).withCodecRegistry(codecRegistry)
  }

}

