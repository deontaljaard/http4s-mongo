package services.auth

import org.http4s.server.middleware.authentication.BasicAuth.1syntax._
import cats.effect._
import io.igl.jwt._
import model.person.Person
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, _}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.joda.time.{DateTime, DateTimeZone}
import services.auth.JwtHelper._

object AuthRs {

  implicit def personEncoder: EntityEncoder[IO, Person] = jsonEncoderOf[IO, Person]

  private def checkCreds(credentials: BasicCredentials): IO[Option[Person]] =
    if(credentials.username == "test") IO.pure(Some(Person("346456456", "Deon", "Taljaard")))
    else IO.pure(None)

  private def authResponse(person: Person): IO[Response[IO]] =
    Ok(person).putHeaders(Header("access_token", buildJwtTokenForPerson(person)))

  val authedService: AuthedService[Person, IO] = AuthedService {
    case POST -> Root / "login" as person =>
      authResponse(person)
  }

  val basicAuthMiddleware = BasicAuth("Test Realm", checkCreds)

  val service = basicAuthMiddleware(authedService)
}

object JwtHelper {
  def getExpThirtyMinutesFromNow: Long =
    DateTime.now(DateTimeZone.UTC).plusMinutes(30).getMillis

  def buildJwtTokenForPerson(person: Person): String =
    new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), Seq(Sub(person.id), Exp(getExpThirtyMinutesFromNow))).encodedAndSigned("secret")
}
