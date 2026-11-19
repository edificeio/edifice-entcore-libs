package org.entcore.broker.api.dto.directory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;

/**
 * Request DTO asking for a user's group ids
 */
public class GetUserGroupIdsRequestDTO {
  private final String userId;

  @JsonCreator
  public GetUserGroupIdsRequestDTO(@JsonProperty("userId") String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  @Transient()
  public boolean isValid() {
    return !StringUtils.isEmpty(userId);
  }

  @Override
  public String toString() {
    return "GetUserGroupIdsRequestDTO{userId='" + userId + "'}";
  }
}
