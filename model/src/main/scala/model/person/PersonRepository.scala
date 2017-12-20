package model.person

import java.util.Date

import fs2.Task
import fs2.Task.fromFuture
import model.mongodb.clients.async.AsyncMongoClientFactory
import model.mongodb.clients.reactive.ReactiveMongoClientFactory
import org.bson.codecs.configuration.CodecProvider
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mongodb.scala.bson.codecs.{Macros => AsyncMacros}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, FindObservable, MongoCollection, SingleObservable}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONObjectID, BSONReader, BSONString, BSONWriter, document, Macros => ReactiveMacros}

import scala.concurrent.Future

trait PersonRepository {
  def findById(objectId: String): Task[Person]

  def insertPerson(person: Person): Task[Person]

  def updatePerson(person: Person): Task[Boolean]

  def deletePerson(objectId: String): Task[Boolean]
}

trait PersonRepositoryComponent {
  val personRepository: PersonRepository

  import common.Implicits.strategy

  import scala.concurrent.ExecutionContext.Implicits.global

  class AsyncMongoPersonRepository extends PersonRepository {

    import org.mongodb.scala.model.Filters._

    case class AsyncMongoPerson(_id: ObjectId, firstName: String, lastName: String)

    object AsyncMongoPerson {
      def fromPerson(person: Person): AsyncMongoPerson =
        AsyncMongoPerson(new ObjectId(), person.firstName, person.lastName)

      def fromPersonWithId(person: Person): AsyncMongoPerson =
        AsyncMongoPerson(new ObjectId(person.id), person.firstName, person.lastName)

      def toPerson(asyncMongoPerson: AsyncMongoPerson): Person =
        Person(asyncMongoPerson._id.toString, asyncMongoPerson.firstName, asyncMongoPerson.lastName)
    }

    import AsyncMongoPerson._

    private val personCodecProvider: CodecProvider = AsyncMacros.createCodecProvider[AsyncMongoPerson]()
    private val personCollection: MongoCollection[AsyncMongoPerson] =
      AsyncMongoClientFactory.getDatabase("demo", Seq(personCodecProvider)).getCollection("person")

    def idEqual(objectId: String): Bson =
      equal("_id", new ObjectId(objectId))

    //TODO: return optional
    def findById(objectId: String): Task[Person] = {
      val observableFind: FindObservable[AsyncMongoPerson] = personCollection.find(idEqual(objectId))

      fromFuture(observableFind.first().head().map(toPerson))
    }

    def insertPerson(person: Person): Task[Person] = {
      val asyncMongoPerson = fromPerson(person)
      val observableInsert: SingleObservable[Completed] = personCollection.insertOne(asyncMongoPerson)

      fromFuture(observableInsert.head().map(_ => toPerson(asyncMongoPerson)))
    }

    def updatePerson(person: Person): Task[Boolean] = {
      val observableUpdate: SingleObservable[UpdateResult] = personCollection.replaceOne(idEqual(person.id), fromPersonWithId(person))

      fromFuture(observableUpdate.head().map(_.wasAcknowledged))
    }

    def deletePerson(objectId: String): Task[Boolean] = {
      val observableDelete: SingleObservable[DeleteResult] = personCollection.deleteOne(idEqual(objectId))

      fromFuture(observableDelete.head().map(_.wasAcknowledged))
    }
  }

  class ReactiveMongoPersonRepository extends PersonRepository {
    val personCollection: Future[BSONCollection] = ReactiveMongoClientFactory.collectionFromDataBase("demo", "person")

    sealed trait Status

    case object Active extends Status

    case object Inactive extends Status

    case class Metadata(status: Status)

    case class ReactiveMongoPerson(_id: BSONObjectID, firstName: String, lastName: String, time: Date = DateTime.now.toDate, metadata: Metadata = Metadata(Active))

    object ReactiveMongoPerson {
      def fromPerson(person: Person): ReactiveMongoPerson =
        ReactiveMongoPerson(BSONObjectID.generate, person.firstName, person.lastName)

      def fromPersonWithId(person: Person): ReactiveMongoPerson =
        ReactiveMongoPerson(BSONObjectID.parse(person.id).get, person.firstName, person.lastName, metadata = Metadata(Inactive))

      def toPerson(reactiveMongoPerson: ReactiveMongoPerson): Person =
        Person(reactiveMongoPerson._id.stringify, reactiveMongoPerson.firstName, reactiveMongoPerson.lastName)
    }

    import ReactiveMongoPerson._

    implicit object TestWriter extends BSONWriter[Status, BSONString] {
      def write(dt: Status): BSONString = BSONString(dt.getClass.getSimpleName.replaceAll("\\$", ""))
    }

    implicit object TestReader extends BSONReader[BSONString, Status] {
      def read(bson: BSONString): Status = bson match {
        case BSONString("EnumVal1") => Active
        case _ => Inactive
      }
    }

    implicit def metadataReader: BSONDocumentReader[Metadata] = ReactiveMacros.reader[Metadata]

    implicit def metadataWriter: BSONDocumentWriter[Metadata] = ReactiveMacros.writer[Metadata]

    implicit def personReader: BSONDocumentReader[ReactiveMongoPerson] = ReactiveMacros.reader[ReactiveMongoPerson]

    implicit def personWriter: BSONDocumentWriter[ReactiveMongoPerson] = ReactiveMacros.writer[ReactiveMongoPerson]

    //TODO: return optional
    override def findById(objectId: String): Task[Person] = {
      val eventualPerson = personCollection.flatMap(_.find(BSONDocument("_id" -> BSONObjectID.parse(objectId).get))
        .one[ReactiveMongoPerson]).map(maybePerson => toPerson(maybePerson.get))

      fromFuture(eventualPerson)
    }

    override def insertPerson(person: Person): Task[Person] = {
      val reactiveMongoPerson = ReactiveMongoPerson.fromPerson(person)

      val eventualPerson = personCollection.flatMap(_.insert(reactiveMongoPerson))
        .flatMap { writeResult =>
          if (writeResult.ok) Future.successful(toPerson(reactiveMongoPerson))
          else Future.failed(new Exception(s"Failed to insert. Reason: ${writeResult.writeErrors}"))
        }

      fromFuture(eventualPerson)
    }

    override def updatePerson(person: Person): Task[Boolean] = {
      val reactiveMongoPerson = fromPersonWithId(person)
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
