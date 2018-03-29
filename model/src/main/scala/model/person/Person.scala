package model.person

import java.util.UUID

case class Person(id: String, firstName: String, lastName: String)

case class PersonNoId(firstName: String, lastName: String)

object Person {

  def toPersonWithId(personNoId: PersonNoId): Person =
    Person(UUID.randomUUID.toString, personNoId.firstName, personNoId.lastName)

}






