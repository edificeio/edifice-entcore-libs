package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for {@link FinalizeManifestRequestDTO}.
 */
public class FinalizeManifestResponseDTO {
  private final boolean manifestOk;
  private final boolean signatureOk;

  @JsonCreator
  public FinalizeManifestResponseDTO(
      @JsonProperty("manifestOk") boolean manifestOk,
      @JsonProperty("signatureOk") boolean signatureOk) {
    this.manifestOk = manifestOk;
    this.signatureOk = signatureOk;
  }

  public boolean isManifestOk() {
    return manifestOk;
  }

  public boolean isSignatureOk() {
    return signatureOk;
  }

  @Override
  public String toString() {
    return "FinalizeManifestResponseDTO{" +
        "manifestOk=" + manifestOk +
        ", signatureOk=" + signatureOk +
        '}';
  }
}
