package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for {@link ListResourceIdsRequestDTO}. {@code nextCursor} is null once the last
 * page has been returned.
 */
public class ListResourceIdsResponseDTO {
  private final List<ResourceRefDTO> resources;
  private final String nextCursor;

  @JsonCreator
  public ListResourceIdsResponseDTO(
      @JsonProperty("resources") List<ResourceRefDTO> resources,
      @JsonProperty("nextCursor") String nextCursor) {
    this.resources = resources;
    this.nextCursor = nextCursor;
  }

  public List<ResourceRefDTO> getResources() {
    return resources;
  }

  public String getNextCursor() {
    return nextCursor;
  }

  @Override
  public String toString() {
    return "ListResourceIdsResponseDTO{" +
        "resources=" + (resources != null ? resources.size() : 0) + " items" +
        ", nextCursor='" + nextCursor + '\'' +
        '}';
  }
}
