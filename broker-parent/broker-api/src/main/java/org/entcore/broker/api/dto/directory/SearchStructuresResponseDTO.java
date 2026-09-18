package org.entcore.broker.api.dto.directory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.entcore.broker.api.dto.directory.structure.FullStructureDTO;

import java.util.List;

public class SearchStructuresResponseDTO {
    private final List<FullStructureDTO> structures;

    @JsonCreator
    public SearchStructuresResponseDTO(@JsonProperty("structures") List<FullStructureDTO> structures) {
        this.structures = structures;
    }

    public List<FullStructureDTO> getStructures() {
        return structures;
    }
}
