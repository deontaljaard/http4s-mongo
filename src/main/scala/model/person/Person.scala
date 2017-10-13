package model.person

import cats.data.Validated.{invalid, valid}
import cats.data.{Validated, NonEmptyList => NEL}
import cats.implicits._
import common.Implicits._
import fs2.Task
import fs2.Task._
import model.mongodb.clients.{AsyncMongoClientFactory, ReactiveMongoClientFactory}
import org.bson.codecs.configuration.CodecProvider
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, FindObservable, MongoCollection, SingleObservable}
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.Future


case class Person(_id: ObjectId = new ObjectId(), firstName: String, lastName: String)
case class PersonNoId(firstName: String, lastName: String)

object Person {
  val personCodecProvider: CodecProvider = Macros.createCodecProvider[Person]()

  def apply(firstName: String, lastName: String): Person =
    Person(new ObjectId(), firstName, lastName)

  def fromPersonNoId(personNoId: PersonNoId): Person =
    apply(personNoId.firstName, personNoId.lastName)

  def validate(person: Person): Validated[NEL[String], String] =
    (validateFirstName(person.firstName) |@|
      validateLastName(person.lastName)) map {_ + _}

  def validateFirstName(firstName: String): Validated[NEL[String], String] =
    if(firstName.length < 6) isInvalid("firstName must be greater than 5 characters")
    else isValid("firstName is all good")

  def validateLastName(lastName: String): Validated[NEL[String], String] =
    if(lastName.length < 4) isInvalid("lastName must be greater than 3 characters")
    else isValid("lastName is all good")

  private def isValid(msg: String): Validated[NEL[String], String] = valid[NEL[String], String](msg)
  private def isInvalid(msg: String) :Validated[NEL[String], String] = invalid[NEL[String], String](NEL.of(msg))

}

object PersonRegistry extends PersonServiceComponent with PersonRepositoryComponent {
  override val personRepository: PersonRepository = new AsyncMongoPersonRepository
  override val personService: PersonService = new PersonServiceImpl
}

trait PersonService extends PersonRepository

trait PersonServiceComponent { this: PersonRepositoryComponent =>
  val personService: PersonService

  class PersonServiceImpl extends PersonService {
    def findById(objectId: String): Task[Person] =
      personRepository.findById(objectId)

    def insertPerson(person: Person): Task[Person] =
      personRepository.insertPerson(person)

    def updatePerson(person: Person): Task[Boolean] =
      personRepository.updatePerson(person)

    def deletePerson(objectId: String): Task[Boolean] =
      personRepository.deletePerson(objectId)
  }

}

trait PersonRepository {
  def findById(objectId: String): Task[Person]

  def insertPerson(person: Person): Task[Person]

  def updatePerson(person: Person): Task[Boolean]

  def deletePerson(objectId: String): Task[Boolean]
}

trait PersonRepositoryComponent {
  val personRepository: PersonRepository

  import org.mongodb.scala.model.Filters._

  import scala.concurrent.ExecutionContext.Implicits.global

  class AsyncMongoPersonRepository extends PersonRepository {

    private val personCodecProvider: CodecProvider = Macros.createCodecProvider[Person]()
    private val collection: MongoCollection[Person] = AsyncMongoClientFactory.getDatabase("a_mydb", personCodecProvider).getCollection("person")

    def idEqual(objectId: String): Bson =
      equal("_id", new ObjectId(objectId))

    //TODO: return optional
    def findById(objectId: String): Task[Person] = {
      val observableFind: FindObservable[Person] = collection.find(idEqual(objectId))

      fromFuture(observableFind.first().head())
    }

    def insertPerson(person: Person): Task[Person] = {
      val observableInsert: SingleObservable[Completed] = collection.insertOne(person)

      fromFuture(observableInsert.head().map(_ => person))
    }

    def updatePerson(person: Person): Task[Boolean] = {
      val observableUpdate: SingleObservable[UpdateResult] = collection.replaceOne(idEqual(person._id.toString), person)

      fromFuture(observableUpdate.head().map(_.wasAcknowledged))
    }

    def deletePerson(objectId: String): Task[Boolean] = {
      val observableDelete: SingleObservable[DeleteResult] = collection.deleteOne(idEqual(objectId))

      fromFuture(observableDelete.head().map(_.wasAcknowledged))
    }
  }

  class ReactiveMongoPersonRepository extends PersonRepository {
    val collection: Future[BSONCollection] = ReactiveMongoClientFactory.collectionFromDataBase("a_mydb", "person")

    override def findById(objectId: String): Task[Person] = ???

    override def insertPerson(person: Person): Task[Person] = ???

    override def updatePerson(person: Person): Task[Boolean] = ???

    override def deletePerson(objectId: String): Task[Boolean] = ???
  }
}

