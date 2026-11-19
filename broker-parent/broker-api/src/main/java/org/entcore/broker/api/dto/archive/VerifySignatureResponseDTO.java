package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for {@link VerifySignatureRequestDTO}. {@code resourceIdsByApp} is only populated
 * when {@code valid} is true (read from {@code Manifest.json})
 */
public class VerifySignatureResponseDTO {
  private final boolean valid;
  private final Map<String, List<String>> resourceIdsByApp;

  @JsonCreator
  public VerifySignatureResponseDTO(
      @JsonProperty("valid") boolean valid,
      @JsonProperty("resourceIdsByApp") Map<String, List<String>> resourceIdsByApp) {
    this.valid = valid;
    this.resourceIdsByApp = resourceIdsByApp;
  }

  public boolean isValid() {
    return valid;
  }

  public Map<String, List<String>> getResourceIdsByApp() {
    return resourceIdsByApp;
  }

  @Override
  public String toString() {
    return "VerifySignatureResponseDTO{" +
        "valid=" + valid +
        ", resourceIdsByApp=" + (resourceIdsByApp != null ? resourceIdsByApp.keySet() : "{}") +
        '}';
  }
}
