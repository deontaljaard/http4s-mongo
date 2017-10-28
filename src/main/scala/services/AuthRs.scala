package services

import fs2.Task
import io.circe.generic.auto._
import io.igl.jwt._
import model.person.Person
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl._
import org.http4s.server.middleware.authentication.BasicAuth
import org.joda.time.{DateTime, DateTimeZone}

object AuthRs {

  implicit def personEncoder: EntityEncoder[Person] = jsonEncoderOf[Person]

  private def checkCreds(credentials: BasicCredentials): Task[Option[Person]] =
    if(credentials.username == "test") Task.now(Some(Person("346456456", "Deon", "Taljaard")))
    else Task.now(None)

  private def getExpThirtyMinutesFromNow: Long =
    DateTime.now(DateTimeZone.UTC).plusMinutes(30).getMillis

  private def buildJwtTokenForPerson(person: Person): String =
    new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), Seq(Sub(person.id), Exp(getExpThirtyMinutesFromNow))).encodedAndSigned("secret")

  private def authResponse(person: Person): Task[Response] =
    Ok(person).putHeaders(Header("access_token", buildJwtTokenForPerson(person)))

  val authedService: AuthedService[Person] = AuthedService {
    case GET -> Root / "login" as person =>
      authResponse(person)
  }

  val basicAuthMiddleware = BasicAuth("Test Realm", checkCreds)

  val service = basicAuthMiddleware(authedService)
}
