package services

import model.person.{PersonRepository, PersonRepositoryComponent, PersonServiceComponent}
import org.specs2.mock.Mockito

trait PersonRegistryTestEnvironment extends PersonServiceComponent with PersonRepositoryComponent with Mockito {
  override val personRepository: PersonRepository = mock[PersonRepository]
  override val personService: PersonServiceImpl = mock[PersonServiceImpl]
}
