package org.entcore.broker.listener;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProbeResponseDTO {
    private final boolean success;
    private final String data;

    @JsonCreator
    public ProbeResponseDTO(@JsonProperty("success") final boolean success,
                            @JsonProperty("data") final String data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getData() {
        return data;
    }
}
