package model.person.validation

import cats.data.ValidatedNel
import cats.implicits._
import model.person._

sealed trait PersonValidatorNel {

  type ValidationResult[A] = ValidatedNel[PersonErrorModel, A]

  private def validateFirstName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNel else FirstNameHasSpecialCharacters.invalidNel

  private def validateLastName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNel else LastNameHasSpecialCharacters.invalidNel

  def validatePersonRegistrationRequest(personRegistrationRequest: PersonRegistrationRequest): ValidationResult[Person] = {
    (validateFirstName(personRegistrationRequest.firstName),
      validateLastName(personRegistrationRequest.lastName))
      .mapN(PersonRegistrationRequest)
      .map(Person.fromPersonRegistrationRequest)
  }
}

object PersonValidatorNel extends PersonValidatorNel

