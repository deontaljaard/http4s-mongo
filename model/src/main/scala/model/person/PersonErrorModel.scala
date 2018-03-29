package model.person

sealed trait PersonErrorModel {
  def errorMessage: String
}

case object FirstNameHasSpecialCharacters extends PersonErrorModel {
  def errorMessage: String = "First name cannot contain spaces, numbers or special characters."
}

case object LastNameHasSpecialCharacters extends PersonErrorModel {
  def errorMessage: String = "Last name cannot contain spaces, numbers or special characters."
}

