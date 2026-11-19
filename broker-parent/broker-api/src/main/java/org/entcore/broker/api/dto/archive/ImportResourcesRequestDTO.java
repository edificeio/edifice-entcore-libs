package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.List;

/**
 * Request DTO asking an application to read a batch of resources (and their attachments)
 * previously written under {@code sourcePrefix}, and import them into {@code targetUserId}'s
 * account.
 */
public class ImportResourcesRequestDTO {
  private final String targetUserId;
  private final String targetUserLogin;
  private final String targetUserDisplayName;
  private final List<String> resourceIds;
  private final String sourcePrefix;

  @JsonCreator
  public ImportResourcesRequestDTO(
      @JsonProperty("targetUserId") String targetUserId,
      @JsonProperty("targetUserLogin") String targetUserLogin,
      @JsonProperty("targetUserDisplayName") String targetUserDisplayName,
      @JsonProperty("resourceIds") List<String> resourceIds,
      @JsonProperty("sourcePrefix") String sourcePrefix) {
    this.targetUserId = targetUserId;
    this.targetUserLogin = targetUserLogin;
    this.targetUserDisplayName = targetUserDisplayName;
    this.resourceIds = resourceIds;
    this.sourcePrefix = sourcePrefix;
  }

  public String getTargetUserId() {
    return targetUserId;
  }

  public String getTargetUserLogin() {
    return targetUserLogin;
  }

  public String getTargetUserDisplayName() {
    return targetUserDisplayName;
  }

  public List<String> getResourceIds() {
    return resourceIds;
  }

  public String getSourcePrefix() {
    return sourcePrefix;
  }

  @Transient()
  public boolean isValid() {
    return !StringUtils.isEmpty(targetUserId)
        && resourceIds != null && !resourceIds.isEmpty()
        && !StringUtils.isEmpty(sourcePrefix);
  }

  @Override
  public String toString() {
    return "ImportResourcesRequestDTO{" +
        "targetUserId='" + targetUserId + '\'' +
        ", targetUserLogin='" + targetUserLogin + '\'' +
        ", targetUserDisplayName='" + targetUserDisplayName + '\'' +
        ", resourceIds=" + (resourceIds != null ? resourceIds.size() : 0) + " items" +
        ", sourcePrefix='" + sourcePrefix + '\'' +
        '}';
  }
}
