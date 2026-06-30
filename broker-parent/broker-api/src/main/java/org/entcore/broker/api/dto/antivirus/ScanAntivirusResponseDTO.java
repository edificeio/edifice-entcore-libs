package org.entcore.broker.api.dto.antivirus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ScanAntivirusResponseDTO {
  public enum Status { ACCEPTED, REJECTED, PARTIAL }
  private final Status status;
  private final String message;
  private final Integer successCount;
  private final List<String> rejectedIds;
  private final Integer errorCount;

  @JsonCreator
  public ScanAntivirusResponseDTO(
    @JsonProperty("status") final Status status,
    @JsonProperty("message") final String message,
    @JsonProperty("successCount") final Integer successCount,
    @JsonProperty("rejectedIds") final List<String> rejectedIds,
    @JsonProperty("errorCount") final Integer errorCount) {
    this.status = status;
    this.message = message;
    this.successCount = successCount;
    this.rejectedIds = rejectedIds;
    this.errorCount = errorCount;
  }

  public Status getStatus() { return status; }
  public String getMessage() { return message; }
  public Integer getSuccessCount() { return successCount; }
  public List<String> getRejectedIds() { return rejectedIds; }
  public Integer getErrorCount() { return errorCount; }
}
