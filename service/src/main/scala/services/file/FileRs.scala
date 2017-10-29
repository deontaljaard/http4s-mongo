package services.file

import org.http4s._
import org.http4s.dsl._
import org.http4s.multipart.Multipart

object FileRs {
  val FILES: String = "files"

  private def processMultipartRequest(request: Request): Unit = {
    println(request.body)
    val multipart = EntityDecoder[Multipart].decode(request, true)
    println(multipart.value.unsafeRunSync)
  }

  val service = HttpService {
    case req @ POST -> Root / FILES / "upload" =>
      val v = for {
        t <- req.as[Multipart]
        _ = processMultipartRequest(req)
      } yield Ok()
      v.flatMap(identity)
  }
}
