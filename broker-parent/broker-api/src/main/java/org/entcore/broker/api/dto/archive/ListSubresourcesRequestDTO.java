package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.Transient;
import java.util.List;

/**
 * Request DTO to list an app's sub-resources (e.g. blog's posts) directly scoped to
 * {@code parentIds}
 * Paginated by cursor, same as {@link ListResourceIdsRequestDTO}.
 */
public class ListSubresourcesRequestDTO {
  private final List<String> parentIds;
  private final String cursor;

  @JsonCreator
  public ListSubresourcesRequestDTO(
      @JsonProperty("parentIds") List<String> parentIds,
      @JsonProperty("cursor") String cursor) {
    this.parentIds = parentIds;
    this.cursor = cursor;
  }

  public List<String> getParentIds() {
    return parentIds;
  }

  public String getCursor() {
    return cursor;
  }

  @Transient()
  public boolean isValid() {
    return parentIds != null && !parentIds.isEmpty();
  }

  @Override
  public String toString() {
    return "ListSubresourcesRequestDTO{" +
        "parentIds=" + (parentIds != null ? parentIds.size() : 0) + " items" +
        ", cursor='" + cursor + '\'' +
        '}';
  }
}
