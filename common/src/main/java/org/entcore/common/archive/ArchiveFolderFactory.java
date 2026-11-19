package org.entcore.common.archive;

import org.entcore.common.storage.Storage;
import org.entcore.common.storage.impl.FileStorage;
import org.entcore.common.storage.impl.S3Storage;

/**
 * Builds an {@link ArchiveFolder} from {@link Storage}
 */
public final class ArchiveFolderFactory {

  private ArchiveFolderFactory() {
  }

  /**
   * @throws IllegalStateException if {@code storage} is neither S3- nor filesystem-backed
   */
  public static ArchiveFolder from(Storage storage) {
    if (storage instanceof S3Storage) {
      final S3Storage s3Storage = (S3Storage) storage;
      return new S3ArchiveFolder(s3Storage.getS3Client(), s3Storage.getS3Client().isFlat());
    }
    if (storage instanceof FileStorage) {
      final FileStorage fileStorage = (FileStorage) storage;
      return new FSArchiveFolder(fileStorage.getFileSystem(), fileStorage.getBucket(), fileStorage.isFlat());
    }
    throw new IllegalStateException(storage.getClass().getSimpleName() + " has no archive storage view");
  }
}
