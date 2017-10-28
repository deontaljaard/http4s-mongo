package model.person

import java.util.UUID

import cats.data.Validated.{invalid, valid, _}
import cats.data.{Validated, NonEmptyList => NEL}
import cats.syntax.cartesian._

case class Person(id: String, firstName: String, lastName: String)

case class PersonNoId(firstName: String, lastName: String)

object Person {

  def toPersonWithId(personNoId: PersonNoId): Person =
    Person(UUID.randomUUID.toString, personNoId.firstName, personNoId.lastName)

  def validate(person: Person): Validated[NEL[String], String] =
    (validateFirstName(person.firstName) |@|
      validateLastName(person.lastName)) map {_ + _}

  def validateFirstName(firstName: String): Validated[NEL[String], String] =
    if (firstName.length < 6) isInvalid("firstName must be greater than 5 characters")
    else isValid("firstName is all good")

  def validateLastName(lastName: String): Validated[NEL[String], String] =
    if (lastName.length < 4) isInvalid("lastName must be greater than 3 characters")
    else isValid("lastName is all good")

  private def isValid(msg: String): Validated[NEL[String], String] = valid[NEL[String], String](msg)

  private def isInvalid(msg: String): Validated[NEL[String], String] = invalid[NEL[String], String](NEL.of(msg))

}






