package services.person

import java.time.Year

import cats.data.{Kleisli, NonEmptyList, OptionT, Validated}
import cats.effect._
import core.person.{PersonRegistry, PersonService}
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import model.person._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import services.Encoders._

import scala.util.Try

object PersonRs {

  def apply(personRegistry: PersonRegistry): PersonRs =
    new PersonRs(personRegistry)

  val PERSONS: String = "persons"

  def getValidationErrors(validationResult: Validated[NonEmptyList[String], String]): String =
    validationResult.fold({ s => s.toList.mkString(", ") }, { _ => "No validation errors" })

  // One can potentially provide context to the error handler: e.g. from GET -> Root, pass in the userId
  private val errorHandler: PartialFunction[Throwable, IO[Response[IO]]] = {
    case e: MatchError => BadRequest(s"The request was probably not well-formed. Msg = ${e.getMessage}")
  }

  implicit val personYearQueryParamMatcher: QueryParamDecoder[Year] =
    QueryParamDecoder[Int].map(Year.of)

  object PersonYearQueryParamMatcher extends QueryParamDecoderMatcher[Year]("year")

  case class CustomUUID(value: String)

  object CustomUUID {
    def unapply(s: String): Option[CustomUUID] =
      Try(Some(CustomUUID(s))).getOrElse(None)
  }

  implicit val personEncoder: EntityEncoder[IO, Person] = jsonEncoderOf[IO, Person]

  implicit val personNoIdEncoder: EntityEncoder[IO, PersonNoId] = jsonEncoderOf[IO, PersonNoId]

  implicit val personDecoder: EntityDecoder[IO, Person] = jsonOf[IO, Person]

  implicit val personNoIdDecoder: EntityDecoder[IO, PersonNoId] = jsonOf[IO, PersonNoId]
}

class PersonRs(personRegistry: PersonRegistry) {

  import PersonRs._

  val personService: PersonService = personRegistry.personService

  type OptionTIO[A] = OptionT[IO, A]

  val personRsService: Kleisli[OptionTIO, Request[IO], Response[IO]] = HttpService[IO] {
    case GET -> Root / PERSONS / personId =>
      personService.findById(personId).flatMap(person => Ok(person.asJson)) //.handleWith(errorHandler)

    case GET -> Root / PERSONS :? PersonYearQueryParamMatcher(year) =>
      Ok(Json.obj("message" -> Json.fromString(s"Looking for person born in $year")))

    case GET -> Root / PERSONS / "extractor" / CustomUUID(id) =>
      Ok(Json.obj("message" -> Json.fromString(s"Custom UUID extractor $id")))

    case req@POST -> Root / PERSONS =>
      for {
        personNoId <- req.as[PersonNoId]
        resp <- personService.insertPerson(Person.toPersonWithId(personNoId)).flatMap(person => Ok(person.asJson))
      } yield resp

    case req@PUT -> Root / PERSONS =>
      for {
        person <- req.as[Person]
        resp <- personService.updatePerson(person).flatMap(person => Ok(person.asJson))
      } yield resp

    case DELETE -> Root / PERSONS / personId =>
      personService.deletePerson(personId).flatMap(Ok(_)) //.handleWith(errorHandler)
  }
}
