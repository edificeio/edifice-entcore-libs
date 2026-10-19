package org.entcore.broker.api.dto.shares;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response indicating whether the user holds the requested share role on the resource.
 */
public class CheckResourceRightResponseDTO {
  private final boolean granted;

  @JsonCreator
  public CheckResourceRightResponseDTO(@JsonProperty("granted") boolean granted) {
    this.granted = granted;
  }

  public boolean isGranted() {
    return granted;
  }
}
