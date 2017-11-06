package core.file

import fs2.Task
import model.file.{FileMetaData, FileStorageRepository}

object AwsS3FileStorageRegistry extends

trait FileStorageService extends FileStorageRepository

trait FileStorageServiceComponent {
  this: FileStorageRepository =>

  val fileStorageService: FileStorageService

  class FileStorageServiceImpl extends FileStorageService {
    override def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): Task[Boolean] =
      uploadObject(fileMetaData, fileData)
  }

}
