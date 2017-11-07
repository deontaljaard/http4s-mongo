package core.file

import model.file.FileRepository
import org.specs2.mock.Mockito

trait FileRegistryTestEnvironment extends FileRegistry
  with Mockito {
  override val fileRepository: FileRepository = mock[FileRepository]
  override val fileService: FileService = mock[FileService]
}
