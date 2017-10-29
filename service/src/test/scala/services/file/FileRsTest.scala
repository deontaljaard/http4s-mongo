package services.file

import fs2._
import org.http4s._
import org.http4s.headers._
import org.specs2.Specification
import org.specs2.matcher.{ThrownExpectations, ThrownMessages}

class FileRsTest extends Specification
  with ThrownExpectations
  with ThrownMessages {

  def is =
    s2"""
   This is a specification to check the 'File RESTful Service'

   The 'File RESTful Service' should
     return a greeting containing the path param               $uploadFile"""

  val fileRs: HttpService = FileRs.service

  def uploadFile = {
    val body =
      """
      ------WebKitFormBoundarycaZFo8IAKVROTEeD
      Content-Disposition: form-data; name="text"
      I AM A MOOSE
      ------WebKitFormBoundarycaZFo8IAKVROTEeD
      Content-Disposition: form-data; name="file1"; filename="Graph_Databases_2e_Neo4j.pdf"
      Content-Type: application/pdf
      ------WebKitFormBoundarycaZFo8IAKVROTEeD
      Content-Disposition: form-data; name="file2"; filename="DataTypesALaCarte.pdf"
      Content-Type: application/pdf
      ------WebKitFormBoundarycaZFo8IAKVROTEeD--
            """.replaceAllLiterally("\n", "\r\n")
    val header = Headers(`Content-Type`(MediaType.multipart("form-data", Some("----WebKitFormBoundarycaZFo8IAKVROTEeD"))))

    val value1: Stream[Task, Byte] = Stream.emit(body).through(text.utf8Encode)


    val uploadRequest = Request(
      method = Method.POST,
      uri = Uri.uri("files/upload"),
      body = value1,
      headers = header)

    val response = fileRs(uploadRequest).unsafeRun

    response.toOption match {
      case Some(resp) =>
        true must_== true
      case None =>
        fail("In uploadFile: no response returned...")
    }
  }
}