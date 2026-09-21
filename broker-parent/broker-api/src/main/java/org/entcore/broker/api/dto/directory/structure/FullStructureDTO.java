package org.entcore.broker.api.dto.directory.structure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

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
    private final Set<FullStructureDTO> parentStructures;

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
                            @JsonProperty("name") String name,
                            @JsonProperty("parentStructures") Set<FullStructureDTO> parentStructures) {
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
        this.parentStructures = parentStructures;
    }

    public Set<FullStructureDTO> getParentStructures() {
        return parentStructures;
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

    @Override
    public String toString() {
        return "FullStructureDTO{" +
                "source='" + source + '\'' +
                ", feederName='" + feederName + '\'' +
                ", postbox='" + postbox + '\'' +
                ", academy='" + academy + '\'' +
                ", ministry='" + ministry + '\'' +
                ", UAI='" + UAI + '\'' +
                ", city='" + city + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", type='" + type + '\'' +
                ", externalId='" + externalId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parentStructures=" + parentStructures +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FullStructureDTO that = (FullStructureDTO) o;
        return Objects.equals(source, that.source) && Objects.equals(feederName, that.feederName) && Objects.equals(postbox, that.postbox) && Objects.equals(academy, that.academy) && Objects.equals(ministry, that.ministry) && Objects.equals(UAI, that.UAI) && Objects.equals(city, that.city) && Objects.equals(zipCode, that.zipCode) && Objects.equals(type, that.type) && Objects.equals(externalId, that.externalId) && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(parentStructures, that.parentStructures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, feederName, postbox, academy, ministry, UAI, city, zipCode, type, externalId, id, name, parentStructures);
    }
}