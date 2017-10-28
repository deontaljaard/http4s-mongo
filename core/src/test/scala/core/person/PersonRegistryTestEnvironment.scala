package core.person

import model.person.{PersonRepository, PersonRepositoryComponent}
import org.specs2.mock.Mockito

trait PersonRegistryTestEnvironment extends PersonRegistry
  with PersonServiceComponent
  with PersonRepositoryComponent
  with Mockito {
  override val personRepository: PersonRepository = mock[PersonRepository]
  override val personService: PersonServiceImpl = mock[PersonServiceImpl]
}
