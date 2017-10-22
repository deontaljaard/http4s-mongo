package model.person

import fs2.Task
import fs2.Task.fromFuture
import model.mongodb.clients.async.AsyncMongoClientFactory
import model.mongodb.clients.reactive.ReactiveMongoClientFactory
import org.bson.codecs.configuration.CodecProvider
import org.bson.types.ObjectId
import org.mongodb.scala.bson.codecs.{Macros => AsyncMacros}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, FindObservable, MongoCollection, SingleObservable}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, document, Macros => ReactiveMacros}
import common.Implicits.strategy
import scala.concurrent.Future

trait PersonRepository {
  def findById(objectId: String): Task[Person]

  def insertPerson(person: Person): Task[Person]

  def updatePerson(person: Person): Task[Boolean]

  def deletePerson(objectId: String): Task[Boolean]
}

trait PersonRepositoryComponent {
  val personRepository: PersonRepository

  import scala.concurrent.ExecutionContext.Implicits.global

  class AsyncMongoPersonRepository extends PersonRepository {
    import org.mongodb.scala.model.Filters._

    case class AsyncMongoPerson(_id: ObjectId, firstName: String, lastName: String)

    object AsyncMongoPerson {
      def apply(person: Person): AsyncMongoPerson =
        AsyncMongoPerson(new ObjectId(), person.firstName, person.lastName)

      def toAsyncMongoPerson(person: Person): AsyncMongoPerson =
        AsyncMongoPerson(new ObjectId(person.id), person.firstName, person.lastName)

      def toPerson(asyncMongoPerson: AsyncMongoPerson): Person =
        Person(asyncMongoPerson._id.toString, asyncMongoPerson.firstName, asyncMongoPerson.lastName)
    }

    import AsyncMongoPerson._

    private val personCodecProvider: CodecProvider = AsyncMacros.createCodecProvider[AsyncMongoPerson]()
    private val personCollection: MongoCollection[AsyncMongoPerson] =
      AsyncMongoClientFactory.getDatabase("a_mydb", personCodecProvider).getCollection("person")

    def idEqual(objectId: String): Bson =
      equal("_id", new ObjectId(objectId))

    //TODO: return optional
    def findById(objectId: String): Task[Person] = {
      val observableFind: FindObservable[AsyncMongoPerson] = personCollection.find(idEqual(objectId))

      fromFuture(observableFind.first().head().map(toPerson))
    }

    def insertPerson(person: Person): Task[Person] = {
      val observableInsert: SingleObservable[Completed] = personCollection.insertOne(AsyncMongoPerson(person))

      fromFuture(observableInsert.head().map(_ => person))
    }

    def updatePerson(person: Person): Task[Boolean] = {
      val observableUpdate: SingleObservable[UpdateResult] = personCollection.replaceOne(idEqual(person.id), toAsyncMongoPerson(person))

      fromFuture(observableUpdate.head().map(_.wasAcknowledged))
    }

    def deletePerson(objectId: String): Task[Boolean] = {
      val observableDelete: SingleObservable[DeleteResult] = personCollection.deleteOne(idEqual(objectId))

      fromFuture(observableDelete.head().map(_.wasAcknowledged))
    }
  }

  class ReactiveMongoPersonRepository extends PersonRepository {
    val personCollection: Future[BSONCollection] = ReactiveMongoClientFactory.collectionFromDataBase("a_mydb", "person")

    case class ReactiveMongoPerson(_id: BSONObjectID, firstName: String, lastName: String)

    object ReactiveMongoPerson {
      def apply(person: Person): ReactiveMongoPerson =
        ReactiveMongoPerson(BSONObjectID.generate, person.firstName, person.lastName)

      def toPerson(reactiveMongoPerson: ReactiveMongoPerson): Person =
        Person(reactiveMongoPerson._id.stringify, reactiveMongoPerson.firstName, reactiveMongoPerson.lastName)

      def toReactiveMongoPerson(person: Person): ReactiveMongoPerson =
        ReactiveMongoPerson(BSONObjectID.parse(person.id).get, person.firstName, person.lastName)
    }

    import ReactiveMongoPerson._

    implicit def personReader: BSONDocumentReader[ReactiveMongoPerson] = ReactiveMacros.reader[ReactiveMongoPerson]
    implicit def personWriter: BSONDocumentWriter[ReactiveMongoPerson] = ReactiveMacros.writer[ReactiveMongoPerson]

    //TODO: return optional
    override def findById(objectId: String): Task[Person] = {
      val eventualPerson = personCollection.flatMap(_.find(BSONDocument("_id" -> BSONObjectID.parse(objectId).get))
        .one[ReactiveMongoPerson]).map(maybePerson => toPerson(maybePerson.get))

      fromFuture(eventualPerson)
    }

    override def insertPerson(person: Person): Task[Person] = {
      val reactiveMongoPerson = ReactiveMongoPerson(person)

      val eventualPerson = personCollection.flatMap(_.insert(reactiveMongoPerson))
        .flatMap { writeResult =>
          if(writeResult.ok) Future.successful(toPerson(reactiveMongoPerson))
          else Future.failed(new Exception(s"Failed to insert. Reason: ${writeResult.writeErrors}"))
        }

      fromFuture(eventualPerson)
    }

    override def updatePerson(person: Person): Task[Boolean] = {
      val reactiveMongoPerson = toReactiveMongoPerson(person)
      val selector = document(
        "_id" -> BSONObjectID.parse(person.id).get,
      )

      val eventualBoolean = personCollection.flatMap(_.update(selector, reactiveMongoPerson))
        .flatMap { updateResult =>
          if (updateResult.ok) Future.successful(true)
          else Future.failed(new Exception(s"Failed to insert. Reason: ${updateResult.writeErrors}"))
        }

      fromFuture(eventualBoolean)
    }

    override def deletePerson(objectId: String): Task[Boolean] = {
      val selector = document(
        "_id" -> BSONObjectID.parse(objectId).get,
      )

      val eventualBoolean = personCollection.flatMap(_.remove(selector))
        .flatMap { writeResult =>
          if (writeResult.ok) Future.successful(true)
          else Future.failed(new Exception(s"Failed to insert. Reason: ${writeResult.writeErrors}"))
        }

      fromFuture(eventualBoolean)
    }
  }

}
