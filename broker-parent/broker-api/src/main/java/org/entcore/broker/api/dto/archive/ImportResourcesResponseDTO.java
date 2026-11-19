package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for {@link ImportResourcesRequestDTO}, mirroring the report already produced by
 * {@code RepositoryEvents.importDocuments} (resourcesNumber/duplicatesNumber/errorsNumber),
 * expressed here as the ids in each bucket instead of a count.
 */
public class ImportResourcesResponseDTO {
  private final List<String> importedIds;
  private final List<String> duplicateIds;
  private final List<String> errorIds;

  @JsonCreator
  public ImportResourcesResponseDTO(
      @JsonProperty("importedIds") List<String> importedIds,
      @JsonProperty("duplicateIds") List<String> duplicateIds,
      @JsonProperty("errorIds") List<String> errorIds) {
    this.importedIds = importedIds;
    this.duplicateIds = duplicateIds;
    this.errorIds = errorIds;
  }

  public List<String> getImportedIds() {
    return importedIds;
  }

  public List<String> getDuplicateIds() {
    return duplicateIds;
  }

  public List<String> getErrorIds() {
    return errorIds;
  }

  @Override
  public String toString() {
    return "ImportResourcesResponseDTO{" +
        "importedIds=" + (importedIds != null ? importedIds.size() : 0) + " items" +
        ", duplicateIds=" + (duplicateIds != null ? duplicateIds.size() : 0) + " items" +
        ", errorIds=" + (errorIds != null ? errorIds.size() : 0) + " items" +
        '}';
  }
}
