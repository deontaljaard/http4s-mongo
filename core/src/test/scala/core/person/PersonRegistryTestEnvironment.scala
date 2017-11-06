package core.person

import model.person.PersonRepository
import org.specs2.mock.Mockito

trait PersonRegistryTestEnvironment extends PersonRegistry
  with Mockito {
  override val personRepository: PersonRepository = mock[PersonRepository]
  override val personService: PersonService = mock[PersonService]
}
