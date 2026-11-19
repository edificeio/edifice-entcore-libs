package org.entcore.broker.api.dto.archive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One resource id, as returned by {@code listResourceIds} and sent back unchanged into
 * {@code exportResources}
 * <p>
 * Examples: 
 * {@code {"id": "a1b2", "type": "blog"}} 
 * {@code {"id": "c3d4", "type": "post"}} 
 */
public class ResourceRefDTO {
  private final String id;
  private final String type;

  @JsonCreator
  public ResourceRefDTO(@JsonProperty("id") String id, @JsonProperty("type") String type) {
    this.id = id;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "ResourceRefDTO{id='" + id + "', type='" + type + "'}";
  }
}
