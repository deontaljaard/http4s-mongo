package services.person

import cats.effect.IO
import core.person.PersonRegistryTestEnvironment
import model.person.Person
import org.http4s.Method._
import org.http4s._
import org.mongodb.scala.bson.ObjectId
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}
import services.RsTestHelper._
import services.person.PersonRs._

class PersonRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages
  with PersonRegistryTestEnvironment {

  def is =
    s2"""
   This is a specification to check the 'Person RESTful Service'

   The 'Person RESTful Service' should
     find a services.person by id                               $findPersonById"""

  val personRs = PersonRs(this).personRsService
  val userId: String = new ObjectId().toString

  val person: Person = Person(userId, "Deon", "Taljaard")
  val eventualPerson: IO[Person] = IO.pure(person)

  // mocks
  personService.findById(anyString) returns eventualPerson

  def findPersonById = {
    val findPersonByIdRequest = Request[IO](GET, buildUrlWithPathParam(PersonRs.PERSONS, person.id))
    val response = personRs(findPersonByIdRequest).value.unsafeRunSync

    response match {
      case Some(resp) =>
        val decodedPerson = resp.as[Person].unsafeRunSync
        decodedPerson.id must_== userId
      case None =>
        fail(s"Expected a decoded person with user id '$userId'")
    }
  }

}
