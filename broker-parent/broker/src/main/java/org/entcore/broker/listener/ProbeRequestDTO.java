package org.entcore.broker.listener;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProbeRequestDTO {
    private final String data;

    @JsonCreator
    public ProbeRequestDTO(@JsonProperty("data") final String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
