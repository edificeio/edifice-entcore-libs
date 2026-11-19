package org.entcore.common.archive;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import org.entcore.common.storage.Storage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ArchiveFolder} backed by a local/mounted filesystem, built by
 * {@link ArchiveFolderFactory#from(org.entcore.common.storage.Storage)} for a filesystem-backed
 * module.
 * <p>
 * Unlike S3, there is no separate "flat" client to switch to: every key here is already a
 * literal path under {@code bucket} (no id-based resharding involved), so a job's hierarchy maps
 * directly onto real subdirectories.
 */
public class FSArchiveFolder implements ArchiveFolder {

  private final FileSystem fs;
  private final String bucket;
  private final boolean normalFlat;

  /**
   * @param fs the module's own {@code vertx.fileSystem()}
   * @param bucket the module's own base path (as returned by {@code Storage.getBucket()})
   * @param normalFlat whether this module's normal (non-job) storage is itself flat - only used
   *        by {@link #resolveStorageKey}; job keys below are always literal, regardless of this
   */
  public FSArchiveFolder(FileSystem fs, String bucket, boolean normalFlat) {
    this.fs = fs;
    this.bucket = bucket;
    this.normalFlat = normalFlat;
  }

  @Override
  public Future<List<String>> listKeys(String prefix) {
    final String root = bucket + prefix;
    return listRecursive(root).map(paths -> {
      final List<String> keys = new ArrayList<>();
      for (String path : paths) {
        keys.add(path.substring(bucket.length()));
      }
      return keys;
    });
  }

  private Future<List<String>> listRecursive(String dir) {
    return fs.exists(dir).compose(exists -> {
      if (!exists) {
        return Future.succeededFuture(Collections.emptyList());
      }
      return fs.readDir(dir).compose(entries -> {
        final List<String> files = new ArrayList<>();
        Future<Void> chain = Future.succeededFuture();
        for (String entry : entries) {
          chain = chain.compose(v -> fs.props(entry).compose(props -> {
            if (props.isDirectory()) {
              return listRecursive(entry).map(sub -> {
                files.addAll(sub);
                return null;
              });
            }
            files.add(entry);
            return Future.succeededFuture();
          }));
        }
        return chain.map(v -> files);
      });
    });
  }

  @Override
  public Future<Buffer> readFile(String key) {
    return fs.readFile(bucket + key);
  }

  @Override
  public Future<String> writeFile(String key, Buffer buffer, String contentType) {
    final String path = bucket + key;
    return fs.mkdirs(parentOf(path)).compose(v -> fs.writeFile(path, buffer)).map(v -> key);
  }

  @Override
  public Future<Void> copyFile(String sourceKey, String destKey) {
    final String destPath = bucket + destKey;
    return fs.mkdirs(parentOf(destPath)).compose(v -> fs.copy(bucket + sourceKey, destPath));
  }

  @Override
  public String resolveStorageKey(String id) {
    try {
      return Storage.getFilePath(id, "", normalFlat);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static String parentOf(String path) {
    final int idx = path.lastIndexOf('/');
    return idx < 0 ? "" : path.substring(0, idx);
  }
}
