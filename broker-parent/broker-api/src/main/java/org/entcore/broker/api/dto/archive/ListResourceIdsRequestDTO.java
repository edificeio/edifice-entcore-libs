package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Request DTO to list the ids of the resources owned by a user or shared via {@code groupIds}
 * Paginated by cursor, a user with a lot of resources is fetched over several calls
 */
public class ListResourceIdsRequestDTO {
  private final String userId;
  private final List<String> groupIds;
  private final Date from;
  private final Date to;
  private final String cursor;

  @JsonCreator
  public ListResourceIdsRequestDTO(
      @JsonProperty("userId") String userId,
      @JsonProperty("groupIds") List<String> groupIds,
      @JsonProperty("from") Date from,
      @JsonProperty("to") Date to,
      @JsonProperty("cursor") String cursor) {
    this.userId = userId;
    this.groupIds = groupIds == null ? Collections.emptyList() : groupIds;
    this.from = from;
    this.to = to;
    this.cursor = cursor;
  }

  public String getUserId() {
    return userId;
  }

  public List<String> getGroupIds() {
    return groupIds;
  }

  public Date getFrom() {
    return from;
  }

  public Date getTo() {
    return to;
  }

  public String getCursor() {
    return cursor;
  }

  @Transient()
  public boolean isValid() {
    return !StringUtils.isEmpty(userId);
  }

  @Override
  public String toString() {
    return "ListResourceIdsRequestDTO{" +
        "userId='" + userId + '\'' +
        ", groupIds=" + groupIds +
        ", from=" + from +
        ", to=" + to +
        ", cursor='" + cursor + '\'' +
        '}';
  }
}
