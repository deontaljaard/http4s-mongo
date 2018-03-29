package model.person

import cats.data.ValidatedNel
import cats.implicits._

sealed trait PersonValidatorNel {

  type ValidationResult[A] = ValidatedNel[PersonErrorModel, A]

  private def validateFirstName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNel else FirstNameHasSpecialCharacters.invalidNel

  private def validateLastName(userName: String): ValidationResult[String] =
    if (userName.matches("^[a-zA-Z0-9]+$")) userName.validNel else LastNameHasSpecialCharacters.invalidNel

  def validateForm(firstName: String, lastName: String): ValidationResult[PersonNoId] = {
    (validateFirstName(firstName),
      validateLastName(lastName)).mapN(PersonNoId)
  }
}

object PersonValidatorNel extends PersonValidatorNel

