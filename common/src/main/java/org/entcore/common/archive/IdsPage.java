package org.entcore.common.archive;

import java.util.List;

/**
 * One page of ids, with whether more are available.
 * <p>
 * Example: paging 12000 ids by 5000 returns {@code new IdsPage(first5000, true)}, then
 * {@code new IdsPage(next5000, true)}, then {@code new IdsPage(last2000, false)}.
 */
public class IdsPage {
  public final List<String> ids;
  public final boolean hasMore;

  public IdsPage(List<String> ids, boolean hasMore) {
    this.ids = ids;
    this.hasMore = hasMore;
  }
}
