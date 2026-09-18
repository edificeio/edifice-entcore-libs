package org.entcore.broker.api.dto.directory.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FullStructureDTO {
    private final String source;
    private final String feederName;
    private final String postbox;
    private final String academy;
    private final String ministry;
    private final String UAI;
    private final String city;
    private final String zipCode;
    private final String type;
    private final String externalId;
    private final String id;
    private final String name;

    @JsonCreator
    public FullStructureDTO(@JsonProperty("source") String source,
                            @JsonProperty("feederName") String feederName,
                            @JsonProperty("postbox") String postbox,
                            @JsonProperty("academy") String academy,
                            @JsonProperty("ministry") String ministry,
                            @JsonProperty("UAI") String UAI,
                            @JsonProperty("city") String city,
                            @JsonProperty("zipCode") String zipCode,
                            @JsonProperty("type") String type,
                            @JsonProperty("externalId") String externalId,
                            @JsonProperty("id") String id,
                            @JsonProperty("name") String name) {
        this.source = source;
        this.feederName = feederName;
        this.postbox = postbox;
        this.academy = academy;
        this.ministry = ministry;
        this.UAI = UAI;
        this.city = city;
        this.zipCode = zipCode;
        this.type = type;
        this.externalId = externalId;
        this.id = id;
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public String getFeederName() {
        return feederName;
    }

    public String getPostbox() {
        return postbox;
    }

    public String getAcademy() {
        return academy;
    }

    public String getMinistry() {
        return ministry;
    }

    public String getUAI() {
        return UAI;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getType() {
        return type;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}