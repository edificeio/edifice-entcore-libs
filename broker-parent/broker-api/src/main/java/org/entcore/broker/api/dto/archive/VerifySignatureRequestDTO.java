package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;

/**
 * Request DTO asking {@code archive} to verify {@code Manifest.json} and {@code archive.signature}
 * written under {@code prefix}
 */
public class VerifySignatureRequestDTO {
  private final String jobId;
  private final String prefix;

  @JsonCreator
  public VerifySignatureRequestDTO(
      @JsonProperty("jobId") String jobId,
      @JsonProperty("prefix") String prefix) {
    this.jobId = jobId;
    this.prefix = prefix;
  }

  public String getJobId() {
    return jobId;
  }

  public String getPrefix() {
    return prefix;
  }

  @Transient()
  public boolean isValid() {
    return !StringUtils.isEmpty(jobId) && !StringUtils.isEmpty(prefix);
  }

  @Override
  public String toString() {
    return "VerifySignatureRequestDTO{" +
        "jobId='" + jobId + '\'' +
        ", prefix='" + prefix + '\'' +
        '}';
  }
}
