package org.entcore.common.archive;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import org.entcore.common.s3.S3Client;
import org.entcore.common.s3.storage.StorageObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * One of two {@link ArchiveFolder} implementations, built by
 * {@link ArchiveFolderFactory#from(org.entcore.common.storage.Storage)} for an S3-backed
 * module.
 * <p>
 * Used flat-keyed (see {@link S3Client#asFlat()}) so a job's prefix is a plain literal key
 */
public class S3ArchiveFolder implements ArchiveFolder {

  private final S3Client client;
  private final boolean normalFlat;

  /**
   * @param client the module's own S3 client (flat or not - this always uses a flat view of it)
   * @param normalFlat whether {@code client} is itself flat, for {@link #resolveStorageKey}
   */
  public S3ArchiveFolder(S3Client client, boolean normalFlat) {
    this.client = client.asFlat();
    this.normalFlat = normalFlat;
  }

  @Override
  public Future<List<String>> listKeys(String prefix) {
    return client.listFilesByPrefix(prefix).map(files ->
        files.stream().map(S3Client.S3FileInfo::getPath).collect(Collectors.toList()));
  }

  @Override
  public Future<Buffer> readFile(String key) {
    final Promise<Buffer> promise = Promise.promise();
    client.readFile(key, res -> {
      if (res.succeeded()) {
        promise.complete(res.result().getBuffer());
      } else {
        promise.fail(res.cause());
      }
    });
    return promise.future();
  }

  @Override
  public Future<String> writeFile(String key, Buffer buffer, String contentType) {
    final Promise<String> promise = Promise.promise();
    client.writeFile(new StorageObject(key, buffer, key, contentType), res -> {
      if (res.succeeded()) {
        promise.complete(res.result());
      } else {
        promise.fail(res.cause());
      }
    });
    return promise.future();
  }

  @Override
  public Future<Void> copyFile(String sourceKey, String destKey) {
    final Promise<Void> promise = Promise.promise();
    client.copyFile(sourceKey, destKey, client.getDefaultBucket(), res -> {
      if (res.succeeded()) {
        promise.complete();
      } else {
        promise.fail(res.cause());
      }
    });
    return promise.future();
  }

  @Override
  public String resolveStorageKey(String id) {
    return S3Client.getPath(id, normalFlat);
  }
}
