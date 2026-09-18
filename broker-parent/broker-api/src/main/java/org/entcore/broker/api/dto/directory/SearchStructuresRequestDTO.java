package org.entcore.broker.api.dto.directory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class SearchStructuresRequestDTO {
    private final String name;
    private final Set<String> ids;

    @JsonCreator
    public SearchStructuresRequestDTO(@JsonProperty("name") String name, @JsonProperty("ids") Set<String> ids) {
        this.name = name;
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public Set<String> getIds() {
        return ids;
    }
}
