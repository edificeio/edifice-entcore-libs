package org.entcore.broker.api.dto.antivirus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ScanAntivirusResponseDTO {
  public enum Status { ACCEPTED, REJECTED, PARTIAL }
  private final Status status;
  private final String message;
  private final Integer count;
  private final List<String> rejectedIds;

  @JsonCreator
  public ScanAntivirusResponseDTO(
    @JsonProperty("status") final Status status,
    @JsonProperty("message") final String message,
    @JsonProperty("count") final Integer count,
    @JsonProperty("rejectedIds") final List<String> rejectedIds) {
    this.status = status;
    this.message = message;
    this.count = count;
    this.rejectedIds = rejectedIds;
  }

  public Status getStatus() { return status; }
  public String getMessage() { return message; }
  public Integer getCount() { return count; }
  public List<String> getRejectedIds() { return rejectedIds; }
}
