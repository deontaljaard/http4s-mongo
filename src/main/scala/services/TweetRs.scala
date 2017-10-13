package services

import fs2.Task
import model.tweet.Tweet
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import common.Implicits.strategy

object TweetRs {
  implicit def tweetEncoder: EntityEncoder[Tweet] = jsonEncoderOf[Tweet]
  implicit def tweetsEncoder: EntityEncoder[Seq[Tweet]] = jsonEncoderOf[Seq[Tweet]]

  def getTweet(tweetId: Int): Task[Tweet] = getTweet

  private def getTweet: Task[Tweet] = Task {
    Tweet(1, "Tweet 1")
  }

  def getPopularTweets(): Task[Seq[Tweet]] = Task {
    Seq(Tweet(1, "Tweet 1"))
  }

  val tweetService = HttpService {
    case GET -> Root / "tweets" / "popular" =>
      Ok(getPopularTweets())
    case GET -> Root / "tweets" / IntVar(tweetId) =>
      getTweet(tweetId).flatMap(Ok(_))
  }
}
