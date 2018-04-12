package model.file

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import model.file.storage.clients.s3.S3ClientFactory

import scala.util.Try
import scala.util.control.NonFatal

trait FileRepository {
  def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): IO[Boolean]
}

trait FileRepositoryComponent extends LazyLogging {
  val fileRepository: FileRepository

  class AwsS3FileRepository extends FileRepository {

    import java.io.ByteArrayInputStream

    import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}

    override def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): IO[Boolean] = {
      IO {
        Try {
          val fileContent = new ByteArrayInputStream(fileData)

          val metadata = new ObjectMetadata
          metadata.setContentLength(fileData.length)

          val putObjectRequest = new PutObjectRequest(S3ClientFactory.s3Bucket, fileMetaData.resourceKey, fileContent, metadata)

          val putObjectResult = S3ClientFactory.s3Client.putObject(putObjectRequest)
          //TODO: work on a cleaner comparison
          putObjectResult.getMetadata.getContentType != ""
        }.recover {
          case NonFatal(e) =>
            logger.error(s"An error occurred while trying to upload file. Reason ${e.getMessage}", e)
            false
        }.getOrElse(false)
      }
    }
  }
}
