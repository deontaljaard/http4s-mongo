package model.person

object AsyncPersonRegistry extends PersonServiceComponent with PersonRepositoryComponent {
  override val personRepository: PersonRepository = new AsyncMongoPersonRepository
  override val personService: PersonService = new PersonServiceImpl
}

object ReactivePersonRegistry extends PersonServiceComponent with PersonRepositoryComponent {
  override val personRepository: PersonRepository = new ReactiveMongoPersonRepository
  override val personService: PersonService = new PersonServiceImpl
}
