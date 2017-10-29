package services

import cats.data.{NonEmptyList, Validated}
import core.person.{PersonRegistry, PersonService}
import fs2.Task
import io.circe.generic.auto._
import model.person._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object PersonRs {

  def apply(personRegistry: PersonRegistry): PersonRs =
    new PersonRs(personRegistry)

  val PERSONS: String = "persons"

  def getValidationErrors(validationResult: Validated[NonEmptyList[String], String]): String =
    validationResult.fold( { s => s.toList.mkString(", ") }, { _ => "No validation errors" } )

  // One can potentially provide context to the error handler: e.g. from GET -> Root, pass in the userId
  private val errorHandler: PartialFunction[Throwable, Task[Response]] = {
    case e: MatchError => BadRequest(s"The request was probably not well-formed. Msg = ${e.getMessage}")
  }
}

class PersonRs(personRegistry: PersonRegistry) {
  import PersonRs._
  import services.Encoders.booleanEncoder
  implicit def personEncoder: EntityEncoder[Person] = jsonEncoderOf[Person]

  val personService: PersonService = personRegistry.personService

  val personRsService = HttpService {
    case GET -> Root / PERSONS / personId =>
      personService.findById(personId).flatMap(Ok(_))//.handleWith(errorHandler)

    case req @ POST -> Root / PERSONS =>
      for {
        personNoId <- req.as(jsonOf[PersonNoId])
        resp <- personService.insertPerson(Person.toPersonWithId(personNoId)).flatMap(Ok(_))
      } yield resp

    case req @ PUT -> Root / PERSONS =>
      for {
        person <- req.as(jsonOf[Person])
        validateResult = Person.validate(person)
        resp <-
        if(validateResult.isValid) personService.updatePerson(person).flatMap(Ok(_))
        else BadRequest(getValidationErrors(validateResult))
      } yield resp

    case DELETE -> Root / PERSONS / personId =>
      personService.deletePerson(personId).flatMap(Ok(_))//.handleWith(errorHandler)
  }
}
