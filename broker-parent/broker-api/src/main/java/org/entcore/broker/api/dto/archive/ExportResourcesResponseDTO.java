package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for {@link ExportResourcesRequestDTO}: which ids of the requested batch were
 * written successfully and which failed
 */
public class ExportResourcesResponseDTO {
  private final List<String> processedIds;
  private final List<String> errorIds;

  @JsonCreator
  public ExportResourcesResponseDTO(
      @JsonProperty("processedIds") List<String> processedIds,
      @JsonProperty("errorIds") List<String> errorIds) {
    this.processedIds = processedIds;
    this.errorIds = errorIds;
  }

  public List<String> getProcessedIds() {
    return processedIds;
  }

  public List<String> getErrorIds() {
    return errorIds;
  }

  @Override
  public String toString() {
    return "ExportResourcesResponseDTO{" +
        "processedIds=" + (processedIds != null ? processedIds.size() : 0) + " items" +
        ", errorIds=" + (errorIds != null ? errorIds.size() : 0) + " items" +
        '}';
  }
}
