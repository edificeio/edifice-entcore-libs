package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Request of the translation overrides of applications, defined in the tenant service as
 * "&lt;application&gt;.i18n.overrides" properties.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetI18nOverridesRequestDTO {

    /** Applications whose overrides are wanted, e.g. "workspace". */
    private final Set<String> applications;

    @JsonCreator
    public GetI18nOverridesRequestDTO(@JsonProperty("applications") Set<String> applications) {
        this.applications = applications;
    }

    public Set<String> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "GetI18nOverridesRequestDTO{applications=" + applications + '}';
    }
}
