package org.entcore.broker.api.dto.directory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class SearchStructuresRequestDTO {
    private final String name;
    private final Set<String> ids;
    private final boolean withParents;

    @JsonCreator
    public SearchStructuresRequestDTO(@JsonProperty("name") String name,
                                      @JsonProperty("ids") Set<String> ids,
                                      @JsonProperty("withParents") boolean withParents) {
        this.name = name;
        this.ids = ids;
        this.withParents = withParents;
    }

    public String getName() {
        return name;
    }

    public Set<String> getIds() {
        return ids;
    }

    public boolean isWithParents() {
        return withParents;
    }
}
