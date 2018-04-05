package services.hello

import org.http4s.Method._
import org.http4s.{HttpService, Request}
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}
import services.RsTestHelper.buildUrlWithPathParam
import cats.effect.IO
import io.circe.generic.auto._

class HelloRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages {

  def is =
    s2"""
   This is a specification to check the 'Hello RESTful Service'

   The 'Hello RESTful Service' should
     return a greeting containing the path param               $returnGreeting"""

  val helloRs: HttpService = HelloRs.helloRsService

  def returnGreeting = {
    val name = "deon"
    val greetingRequest = GET(buildUrlWithPathParam(HelloRs.HELLO, name))//Request[IO](GET, buildUrlWithPathParam(HelloRs.HELLO, name))
    val response = helloRs(greetingRequest).unsafeRun

    response.toOption match {
      case Some(resp) =>
        resp.as[String].unsafeRun.contains(s"Hello, $name") must_== true
      case None =>
        fail("In returnGreeting: no response returned...")
    }
  }
}
