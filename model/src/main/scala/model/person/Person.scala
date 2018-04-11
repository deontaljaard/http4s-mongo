package model.person

case class Person(id: String = "", firstName: String, lastName: String)

case class PersonRegistrationRequest(firstName: String, lastName: String)

object Person {

  def fromPersonRegistrationRequest(personRegistrationRequest: PersonRegistrationRequest): Person =
    Person(
      firstName = personRegistrationRequest.firstName,
      lastName = personRegistrationRequest.lastName
    )

}






