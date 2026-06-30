package org.entcore.broker.api.dto.antivirus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanAntivirusRequestDTO {
  private final List<String> ids;

  @JsonCreator
  public ScanAntivirusRequestDTO(
    @JsonProperty("ids") final List<String> ids) {
    this.ids = ids;
  }

  public List<String> getIds() { return ids; }

  @Override
  public String toString() {
    return "ScanAntivirusRequestDTO{" +
      ", ids=" + ids +
      '}';
  }
}
