package services.file

import java.nio.charset.CharacterCodingException

import com.typesafe.scalalogging.LazyLogging
import core.file.{FileRegistry, FileService}
import fs2.Task
import io.circe.generic.auto._
import io.circe.parser._
import model.file.FileMetaData
import org.http4s.MediaType._
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl._
import org.http4s.headers._
import org.http4s.multipart.{Multipart, Part}
import scodec.bits.ByteVector
import services.file.FileRs.{FILES, JsonMetaData, MultipartResponse}
import services.file.MultipartHelper._

object FileRs {

  def apply(fileRegistry: FileRegistry): FileRs =
    new FileRs(fileRegistry)

  val FILES: String = "files"

  case class JsonMetaData(userId: String)

  case class MultipartResponse(userId: String, fileName: String, successful: Boolean)

  object MultipartResponse {
    def empty: MultipartResponse = MultipartResponse("", "", successful = false)
  }

}

class FileRs(fileRegistry: FileRegistry) extends LazyLogging {

  implicit def jsonMetaDataEncoder: EntityEncoder[JsonMetaData] = jsonEncoderOf[JsonMetaData]

  implicit def multipartResponseEncoder: EntityEncoder[MultipartResponse] = jsonEncoderOf[MultipartResponse]

  private val fileService: FileService = fileRegistry.fileService

  val fileRsService = HttpService {
    case req@POST -> Root / FILES / "upload" =>
      for {
        t <- req.as[Multipart]
        resp <- processMultipart(t).flatMap(Ok(_))
      } yield resp
  }

  private def processMultipart(multipart: Multipart): Task[MultipartResponse] = {

    val parts: Vector[Part] = multipart.parts

    parts.foldLeft(Task.delay(MultipartResponse.empty)) { case (eventualResponse, part) =>
      part.headers.get(`Content-Type`) match {
        case Some(`Content-Type`(`application/json`, _)) =>
          logger.debug("Process JSON payload")
          processJsonMetaDataPart(eventualResponse, part)
        case Some(`Content-Type`(`image/png`, _)) =>
          logger.debug("Process image")
          processFilePart(eventualResponse, part)
        case unknownContentType =>
          logger.error(s"Unsupported content type in multipart request - '$unknownContentType'. Ignoring.")
          eventualResponse
      }
    }
  }

  private def processJsonMetaDataPart(eventualResponse: Task[MultipartResponse], part: Part): Task[MultipartResponse] = {
    val updateResponse: Option[JsonMetaData] => Task[MultipartResponse] =
      maybeJsonMetaData => eventualResponse.map(_.copy(userId = maybeJsonMetaData.map(_.userId).getOrElse("")))

    for {
      byteVector <- partToByteVector(part)
      decodedJsonString = byteVector.decodeUtf8
      maybeJsonMetaData = decodedJsonString.fold(handleCodingException, parseJsonString)
      response <- updateResponse(maybeJsonMetaData)
    } yield {
      response
    }
  }

  private def processFilePart(eventualResponse: Task[MultipartResponse], part: Part): Task[MultipartResponse] = {
    val updateResponse: (String, Boolean) => Task[MultipartResponse] =
      (fileName, successful) => eventualResponse.map(_.copy(fileName = fileName, successful = successful))

    for {
      byteVector <- partToByteVector(part)
      fileData = byteVector.toArray
      partName = part.name.get
      partialResponse <- eventualResponse
      successful <- fileService.uploadObject(FileMetaData(partialResponse.userId), fileData)
      response <- updateResponse(partName, successful)
    } yield {
      response
    }
  }

}

object MultipartHelper {
  val handleCodingException: CharacterCodingException => Option[JsonMetaData] =
    ex => {
      println(ex)
      None
    }

  val partToByteVector: Part => Task[ByteVector] =
    _.body.runLog.map(ByteVector(_))

  //TODO: Parameterize this function
  val decodeJsonString: String => Option[JsonMetaData] =
    json => decode[JsonMetaData](json) match {
      case Left(error) => println(error)
        None
      case Right(js: JsonMetaData) => Some(js)
    }

  //TODO: Parameterize this function
  val parseJsonString: String => Option[JsonMetaData] =
    json => parse(json) match {
      case Left(e) => println(e)
        None
      case Right(_) => decodeJsonString(json)
    }

}
