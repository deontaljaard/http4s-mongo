package server

import java.util.concurrent.{ExecutorService, Executors}

import cats.implicits._
import fs2.Task
import org.http4s.HttpService
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import services.{HelloWorld, PersonRs, TweetRs}
import scala.util.Properties.envOrNone

//object BlazeExample extends ServerApp {
//
//  val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
//  val ip   : String           = "0.0.0.0"
//  val pool : ExecutorService  = Executors.newCachedThreadPool()
//
//  override def server(args: List[String]): Task[Server] =
//    BlazeBuilder
//      .bindHttp(port, ip)
//      .mountService(HelloWorld.service)
//      .mountService(Tweet.tweetService)
//      .withServiceExecutor(pool)
//      .start
//}

object BlazeExample extends StreamApp {

  val port : Int              = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 8080
  val ip   : String           = "0.0.0.0"
  val pool : ExecutorService  = Executors.newCachedThreadPool()

  val services: HttpService = HelloWorld.service |+| TweetRs.tweetService |+| PersonRs.service

  override def stream(args: List[String]): fs2.Stream[Task, Nothing] = {
    BlazeBuilder
    .bindHttp(8080, "localhost")
    .mountService(services, "/api")
    .serve
  }
}
