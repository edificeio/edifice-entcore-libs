package org.entcore.broker.api.dto.antivirus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanAntivirusRequestDTO {
  public enum Mode { S3, LEGACY }
  private final Mode mode;
  private final List<String> ids;
  private final String bucket;

  @JsonCreator
  public ScanAntivirusRequestDTO(
    @JsonProperty("mode") final Mode mode,
    @JsonProperty("ids") final List<String> ids,
    @JsonProperty("bucket") final String bucket) {
    this.mode = mode;
    this.ids = ids;
    this.bucket = bucket;
  }

  public Mode getMode() { return mode; }
  public List<String> getIds() { return ids; }
  public String getBucket() { return bucket; }

  @Override
  public String toString() {
    return "ScanAntivirusRequestDTO{" +
      "mode=" + mode +
      ", ids=" + ids +
      ", bucket=" + bucket +
      '}';
  }
}
