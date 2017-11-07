package services.person

import common.Implicits.strategy
import core.person.PersonRegistryTestEnvironment
import fs2.Task
import io.circe.generic.auto._
import model.person.Person
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.jsonOf
import org.mongodb.scala.bson.ObjectId
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}
import services.PersonRs
import services.RsTestHelper._

class PersonRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages
  with PersonRegistryTestEnvironment {

  def is =
    s2"""
   This is a specification to check the 'Person RESTful Service'

   The 'Person RESTful Service' should
     find a services.person by id                               $findPersonById"""

  val personRs: HttpService = PersonRs(this).personRsService
  val userId: String = new ObjectId().toString

  val person: Person = Person(userId, "Deon", "Taljaard")
  val eventualPerson: Task[Person] = Task(person)

  // mocks
  personService.findById(anyString) returns eventualPerson

  def findPersonById = {
    val findPersonByIdRequest = Request(GET, buildUrlWithPathParam(PersonRs.PERSONS, person.id))
    val response = personRs(findPersonByIdRequest).unsafeRun

    response.toOption match {
      case Some(resp) =>
        val decodedPerson = resp.as(jsonOf[Person]).unsafeRun
        decodedPerson.id must_== userId
      case None =>
        fail(s"Expected a decoded person with user id '$userId'")
    }
  }

}
