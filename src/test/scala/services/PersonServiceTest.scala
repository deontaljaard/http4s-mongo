package services

import fs2.Task
import model.person.Person
import org.mongodb.scala.bson.ObjectId
import org.specs2.Specification
import common.Implicits.strategy

class PersonServiceTest extends Specification
  with PersonRegistryTestEnvironment {
  def is = s2"""
   This is a specification to check the 'Person Service Component'

   The 'Person Service Component' should
     find a person by id                                            $findPersonById"""

  override val personService = new PersonServiceImpl

  val personId = new ObjectId().toString
  val eventualPerson = Task(Person(personId, "Deon", "Taljaard"))

  // mocks
  personRepository.findById(anyString) returns eventualPerson

  def findPersonById = {
    personService.findById(personId) must_== eventualPerson
    there was one(personRepository).findById(anyString)
  }

}
