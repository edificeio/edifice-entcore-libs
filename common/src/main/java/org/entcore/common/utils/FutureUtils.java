package org.entcore.common.utils;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FutureUtils {

  private FutureUtils() {
  }

  /**
   * Applies {@code fn} to every item of {@code items}, {@code batchSize} at a time.
   * <p>
   * Each batch runs concurrently (via {@link CompositeFuture#all(List)}); batches run one after
   * another. A middle ground between fully sequential ({@code batchSize=1}) and fully unbounded
   * concurrency ({@code batchSize=items.size()}), for work items that are each one or more
   * network round trips (DB/S3) and would otherwise add up to a slow, fully sequential total
   * latency.
   * <p>
   * Example: {@code inBatches([a,b,c,d,e], 2, fn)} runs {@code fn(a)}/{@code fn(b)} concurrently,
   * then {@code fn(c)}/{@code fn(d)}, then {@code fn(e)} - never more than 2 at once.
   *
   * @param items the items to process
   * @param batchSize how many items to run concurrently per batch
   * @param fn the work to run for each item
   * @return the results, in the same order as {@code items}
   */
  public static <T, R> Future<List<R>> inBatches(List<T> items, int batchSize, Function<T, Future<R>> fn) {
    final List<R> results = new ArrayList<>();
    Future<Void> chain = Future.succeededFuture();
    for (int i = 0; i < items.size(); i += batchSize) {
      final List<T> batch = items.subList(i, Math.min(i + batchSize, items.size()));
      chain = chain.compose(v -> {
        final List<Future<R>> batchFutures = new ArrayList<>();
        for (T item : batch) {
          batchFutures.add(fn.apply(item));
        }
        return CompositeFuture.all(new ArrayList<>(batchFutures)).map(composite -> {
          for (int i2 = 0; i2 < batchFutures.size(); i2++) {
            results.add(composite.resultAt(i2));
          }
          return null;
        });
      });
    }
    return chain.map(v -> results);
  }
}
