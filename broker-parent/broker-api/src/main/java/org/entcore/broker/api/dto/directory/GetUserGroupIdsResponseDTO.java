package org.entcore.broker.api.dto.directory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for {@link GetUserGroupIdsRequestDTO}. Empty (not an error) if the user can't be
 * resolved to a session.
 */
public class GetUserGroupIdsResponseDTO {
  private final List<String> groupIds;

  @JsonCreator
  public GetUserGroupIdsResponseDTO(@JsonProperty("groupIds") List<String> groupIds) {
    this.groupIds = groupIds;
  }

  public List<String> getGroupIds() {
    return groupIds;
  }

  @Override
  public String toString() {
    return "GetUserGroupIdsResponseDTO{groupIds=" + groupIds + "}";
  }
}
