package services

import org.http4s.Uri
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}

object RsTestHelper extends ThrownExpectations
  with ThrownMessages {

  def buildUrlWithPathParam(resource: String, pathParam: String): Uri =
    Uri.fromString(s"$resource/$pathParam")
      .fold({ uri => fail(s"Invalid URI. Reason: ${uri.message}") }, { uri => uri })

}
