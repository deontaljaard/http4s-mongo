package core.file

import cats.effect.IO
import model.file.{FileMetaData, FileRepository, FileRepositoryComponent}

trait FileRegistry extends FileServiceComponent with FileRepositoryComponent

object AwsS3FileRegistry extends FileRegistry {
  override val fileRepository: FileRepository = new AwsS3FileRepository
  override val fileService: FileService = new FileServiceImpl
}

trait FileService extends FileRepository

trait FileServiceComponent {
  this: FileRepositoryComponent =>

  val fileService: FileService

  class FileServiceImpl extends FileService {
    override def uploadObject(fileMetaData: FileMetaData, fileData: Array[Byte]): IO[Boolean] =
      fileRepository.uploadObject(fileMetaData, fileData)
  }

}
