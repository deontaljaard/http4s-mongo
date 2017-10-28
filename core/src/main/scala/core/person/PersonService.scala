package core.person

import fs2.Task
import model.person.{Person, PersonRepository, PersonRepositoryComponent}

trait PersonRegistry extends PersonServiceComponent with PersonRepositoryComponent

object AsyncPersonRegistry extends PersonRegistry {
  override val personRepository: PersonRepository = new AsyncMongoPersonRepository
  override val personService: PersonService = new PersonServiceImpl
}

object ReactivePersonRegistry extends PersonRegistry {
  override val personRepository: PersonRepository = new ReactiveMongoPersonRepository
  override val personService: PersonService = new PersonServiceImpl
}

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