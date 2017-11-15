package services.file

import java.io.File

import core.file.FileRegistryTestEnvironment
import fs2.Task
import io.circe.generic.auto._
import io.circe.syntax._
import model.file.FileMetaData
import org.http4s.MediaType._
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers._
import org.http4s.multipart.{Multipart, Part}
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}
import services.file.FileRs.{JsonMetaData, MultipartResponse}

class FileRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages
  with FileRegistryTestEnvironment {

  def is =
    s2"""
   This is a specification to check the 'File RESTful Service'

   The 'File RESTful Service' should
    return a successful multi part response              $uploadFile"""

  val fileRs: HttpService = FileRs(this).fileRsService

  // mocks
  fileService.uploadObject(any[FileMetaData], any[Array[Byte]]) returns Task.now(true)

  def uploadFile = {
    implicit def jsonMetaDataEncoder: EntityEncoder[JsonMetaData] = jsonEncoderOf[JsonMetaData]

    implicit def multipartResponseEncoder: EntityEncoder[MultipartResponse] = jsonEncoderOf[MultipartResponse]

    val path = getClass.getResource("/http4s.png").getPath
    val file = new File(path)
    val multipart = buildMultipartWithFile(file)
    val multipartEntity = EntityEncoder[Multipart].toEntity(multipart)
    val multipartBody = multipartEntity.unsafeRun.body

    val uploadRequest = Request(
      method = Method.POST,
      uri = Uri.uri("files/upload"),
      body = multipartBody,
      headers = multipart.headers)

    val response = fileRs(uploadRequest).unsafeRun

    response.toOption match {
      case Some(resp) =>
        val multipartResponse = resp.as(jsonOf[MultipartResponse]).unsafeRun
        multipartResponse.fileName must_== file.getName
        multipartResponse.successful must_== true
      case None =>
        fail("In uploadFile: no response returned...")
    }
  }

  private def buildMultipartWithFile(file: File): Multipart = {
    val jsonMetaData = JsonMetaData("76293429")
    val json = jsonMetaData.asJson.toString
    val part1 = Part.formData("test-metadata", json, `Content-Type`(`application/json`))
    val part2 = Part.fileData(file.getName, file, `Content-Type`(`image/png`))
    Multipart(Vector(part1, part2))
  }

}