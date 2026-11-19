package org.entcore.common.archive;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.util.List;

/**
 * Read/write access to an archive folder, by arbitrary literal key - independent of a module's
 * own {@link org.entcore.common.storage.Storage} implementation.
 * <p>
 * Every key below ({@code listKeys}/{@code readFile}/{@code writeFile}/{@code copyFile}) is
 * always treated literally, never resharded - a job's hierarchical prefix (e.g.
 * {@code job42/blog/post_x}) would otherwise be destroyed. Only {@link #resolveStorageKey}
 * depends on the module's own (possibly sharded) storage convention, since it resolves an
 * existing, non-job object - there is no "non-flat" {@code ArchiveFolder}.
 */
public interface ArchiveFolder {

  /**
   * Lists every object's key under {@code prefix}, recursively.
   * <p>
   * Example: for
   * <pre>
   * job42/
   *   blog/
   *     post_x
   *     Documents/
   *       img_y
   * </pre>
   * {@code listKeys("job42")} returns [job42/blog/post_x, job42/blog/Documents/img_y]
   *
   * @param prefix the S3 prefix to list under
   * @return the full key (path) of each matching object
   */
  Future<List<String>> listKeys(String prefix);

  /**
   * Reads one file content.
   *
   * @param key the object's full key
   */
  Future<Buffer> readFile(String key);

  /**
   * Writes one file.
   *
   * @param key the object's full key
   * @return the key that was written
   */
  Future<String> writeFile(String key, Buffer buffer, String contentType);

  /**
   * Copy from one key to another.
   *
   * @param sourceKey an already-resolved key, not an id
   * @param destKey an already-resolved key, not an id
   */
  Future<Void> copyFile(String sourceKey, String destKey);

  /**
   * Resolves the real storage key for a file
   * <p>
   * Example: for id {@code "11111111-2222-3333-4444-aabbccddeeff"}:
   * <pre>
   * flat:    11111111-2222-3333-4444-aabbccddeeff
   * sharded: ff/ee/11111111-2222-3333-4444-aabbccddeeff
   * </pre>
   *
   * @param id the object's logical id
   * @return its real storage key
   */
  String resolveStorageKey(String id);
}
