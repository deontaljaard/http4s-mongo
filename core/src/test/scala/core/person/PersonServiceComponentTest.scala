package core.person

import cats.effect.IO
import model.person.Person
import org.mongodb.scala.bson.ObjectId
import org.specs2.Specification

class PersonServiceComponentTest extends Specification
  with PersonRegistryTestEnvironment {
  def is = s2"""
   This is a specification to check the 'Person Service Component'

   The 'Person Service Component' should
     find a services.person by id                                            $findPersonById"""

  override val personService = new PersonServiceImpl

  val personId = new ObjectId().toString
  val eventualPerson = IO.pure(Person(personId, "Deon", "Taljaard"))

  // mocks
  personRepository.findById(anyString) returns eventualPerson

  def findPersonById = {
    personService.findById(personId) must_== eventualPerson
    there was one(personRepository).findById(anyString)
  }

}
