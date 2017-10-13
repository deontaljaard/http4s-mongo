package services

import common.Implicits.strategy
import fs2.Task
import io.circe.generic.auto._
import model.person.Person
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.jsonOf
import org.mongodb.scala.bson.ObjectId
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}

class PersonRsTest extends Specification
  with ThrownExpectations with ThrownMessages
  with PersonRegistryTestEnvironment {

  def is =
    s2"""
   This is a specification to check the 'Person Restful Service'

   The 'Person Restful Service' should
     find a person by id                                            $findPersonById"""

  val personRs = new PersonRs(this).personRsService
  val userId = new ObjectId().toString


  val findPersonByIdRequest = Request(GET, buildUrlWithPathParam(PersonRs.PERSONS, userId))
  val person = Person(new ObjectId(userId), "Deon", "Taljaard")
  val eventualPerson = Task(person)

  // mocks
  personService.findById(anyString) returns eventualPerson

  def findPersonById = {
    import model.mongodb.MongoOps._

    val response = personRs(findPersonByIdRequest).unsafeRun

    val result = response.toOption match {
      case Some(resp) =>
        val eventualDecodedPerson = for {
          p <- resp.as(jsonOf[Person])
        } yield {
          p == person
        }
        eventualDecodedPerson.unsafeRun
      case None => false
    }

    result must_== true
  }

  private def buildUrlWithPathParam(resource: String, pathParam: String): Uri =
    Uri.fromString(s"$resource/$pathParam").fold({ uri => fail(s"Invalid URI. Reason: ${uri.message}") }, { uri => uri })

}
