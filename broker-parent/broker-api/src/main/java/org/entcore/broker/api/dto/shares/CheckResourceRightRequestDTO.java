package org.entcore.broker.api.dto.shares;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import java.beans.Transient;

/**
 * Request to check whether a user holds a share role (read, contrib...) on a resource,
 * either directly or through one of his groups.
 * Resource ownership is not checked.
 */
public class CheckResourceRightRequestDTO {
  private final String userId;
  private final String resourceId;
  /**
   * Share role to check ("contrib"...)
   */
  private final String role;

  @JsonCreator
  public CheckResourceRightRequestDTO(@JsonProperty("userId") String userId, @JsonProperty("resourceId") String resourceId, @JsonProperty("role") String role) {
    this.userId = userId;
    this.resourceId = resourceId;
    this.role = role;
  }

  public String getUserId() {
    return userId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getRole() {
    return role;
  }

  @Transient()
  public boolean isValid() {
    return StringUtils.isNotEmpty(userId)
            && StringUtils.isNotEmpty(resourceId)
            && StringUtils.isNotEmpty(role);
  }

  @Override
  public String toString() {
    return "CheckResourceRightRequestDTO{" +
            "userId='" + userId + '\'' +
            ", resourceId='" + resourceId + '\'' +
            ", role='" + role + '\'' +
            '}';
  }
}
