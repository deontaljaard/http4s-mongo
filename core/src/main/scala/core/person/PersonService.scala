package core.person

import cats.effect.IO
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
    def findById(objectId: String): IO[Person] =
      personRepository.findById(objectId)

    def insertPerson(person: Person): IO[Person] =
      personRepository.insertPerson(person)

    def updatePerson(person: Person): IO[Boolean] =
      personRepository.updatePerson(person)

    def deletePerson(objectId: String): IO[Boolean] =
      personRepository.deletePerson(objectId)
  }
}