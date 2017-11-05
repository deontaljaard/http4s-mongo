package services.file

import java.nio.charset.CharacterCodingException

import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.headers._
import fs2.Task
import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.circe.jsonEncoderOf
import org.http4s.multipart.{Boundary, Multipart, Part}
import scodec.bits.ByteVector
import io.circe._, io.circe.parser._

object FileRs {
  val FILES: String = "files"

  case class JsonMetaData(userId: String)

  case class MultipartResponse(userId: String, fileName: String, contentType: `Content-Type`)

  val service = HttpService {
    case req@POST -> Root / FILES / "upload" =>
      val v = for {
        t <- req.as[Multipart]
        _ = processMultipart(t)
      } yield Ok()
      v.flatMap(identity)
  }

  private def processMultipart(multipart: Multipart): Unit = {

    implicit def jsonMetaDataEncoder: EntityEncoder[JsonMetaData] = jsonEncoderOf[JsonMetaData]

    val headers: Headers = multipart.headers
    println(s"Headers: $headers")

    val boundary: Boundary = multipart.boundary
    println(s"boundary: $boundary")

    val parts: Vector[Part] = multipart.parts
    println(s"Parts: $parts")

    val vector = parts.map { part =>
      part.headers.get(`Content-Type`) match {
        case Some(`Content-Type`(`application/json`, _)) =>
          println("Process JSON payload")
          val jsonAsString = part.body.runLog.map(ByteVector(_))
            .map(_.decodeUtf8)
            .map(_.fold(g
              { ex => println(ex); None },
              { s =>
                parse(s) match {
                  case Left(e) => println(e); None
                  case Right(_) => decode[JsonMetaData](s) match {
                    case Left(error) => println(error); None
                    case Right(js: JsonMetaData) => Some(js)
                  }
                }
              })
            )
          println(s"---- ${jsonAsString.unsafeRun}")
        case Some(`Content-Type`(`image/png`, _)) =>
          println("Process image")
          val imageByteVector: Task[ByteVector] = part.body.runLog.map(ByteVector(_))
          println(s"Image byte vector ${imageByteVector.unsafeRun}")
          val imageByteArray = imageByteVector.map(_.toArray)
          println(s"Image byte array ${imageByteArray.unsafeRun.length}")
        case _ => println("Unsupported content type in part. Ignoring.")
      }
      val value: Task[Either[CharacterCodingException, String]] = part.body.runLog.map(ByteVector(_)).map(_.decodeUtf8)
      value.unsafeValue
    }

    println("Decoded multipart" + vector)
  }

  private def processMultipartRequest(request: Request): Unit = {
    println(request.body)
    val multipart = EntityDecoder[Multipart].decode(request, strict = true)
    println(multipart.value.unsafeRunSync)
  }
}
