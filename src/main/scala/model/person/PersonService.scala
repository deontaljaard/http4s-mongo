package model.person

import fs2.Task

trait PersonService extends PersonRepository

trait PersonServiceComponent {
  this: PersonRepositoryComponent =>
  val personService: PersonService

  class PersonServiceImpl extends PersonService {
    def findById(objectId: String): Task[Person] =
      personRepository.findById(objectId)

    def insertPerson(person: Person): Task[Person] =
      personRepository.insertPerson(person)

    def updatePerson(person: Person): Task[Boolean] =
      personRepository.updatePerson(person)

    def deletePerson(objectId: String): Task[Boolean] =
      personRepository.deletePerson(objectId)
  }

}