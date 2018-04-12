package services.hello

import cats.effect._
import org.http4s.Method._
import org.http4s._
import org.specs2.Specification
import org.specs2.matcher._
import services.RsTestHelper.buildUrlWithPathParam

class HelloRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages {

  def is =
    s2"""
   This is a specification to check the 'Hello RESTful Service'

   The 'Hello RESTful Service' should
     return a greeting containing the path param               $returnGreeting"""

  val helloRs = HelloRs.helloRsService

  def returnGreeting = {
    val name = "deon"
    val greetingRequest = Request[IO](GET, buildUrlWithPathParam(HelloRs.HELLO, name))
    val response = helloRs(greetingRequest).value.unsafeRunSync

    response match {
      case Some(resp) =>
        resp.as[String].unsafeRunSync.contains(s"Hello, $name") must_== true
      case None =>
        fail("In returnGreeting: no response returned...")
    }
  }
}
