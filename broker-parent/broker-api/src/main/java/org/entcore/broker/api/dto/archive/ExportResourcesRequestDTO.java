package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.List;

/**
 * Request DTO asking an application to write a batch of its own resources (and their
 * attachments) as individual S3 objects under {@code prefix}.
 */
public class ExportResourcesRequestDTO {
  private final List<ResourceRefDTO> resources;
  private final String prefix;

  @JsonCreator
  public ExportResourcesRequestDTO(
      @JsonProperty("resources") List<ResourceRefDTO> resources,
      @JsonProperty("prefix") String prefix) {
    this.resources = resources;
    this.prefix = prefix;
  }

  public List<ResourceRefDTO> getResources() {
    return resources;
  }

  public String getPrefix() {
    return prefix;
  }

  @Transient()
  public boolean isValid() {
    return resources != null && !resources.isEmpty() && !StringUtils.isEmpty(prefix);
  }

  @Override
  public String toString() {
    return "ExportResourcesRequestDTO{" +
        "resources=" + (resources != null ? resources.size() : 0) + " items" +
        ", prefix='" + prefix + '\'' +
        '}';
  }
}
