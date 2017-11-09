package model.file

import com.typesafe.scalalogging.{LazyLogging, Logger}
import fs2.Task
import model.file.storage.clients.s3.S3ClientFactory

import scala.util.Try
import scala.util.control.NonFatal

trait FileRepository {
  def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): Task[Boolean]
}

trait FileRepositoryComponent extends LazyLogging {
  val fileRepository: FileRepository

  class AwsS3FileRepository extends FileRepository {

    import com.amazonaws.services.s3.model.ObjectMetadata
    import com.amazonaws.services.s3.model.PutObjectRequest
    import java.io.ByteArrayInputStream

    override def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): Task[Boolean] = {
      Task.delay {
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
