package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/** Notifies that translation overrides changed, for their consumers to reload them. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class I18nOverridesChangedDTO {

    /** The applications whose overrides changed; empty when it may concern all of them */
    private final Set<String> applications;

    @JsonCreator
    public I18nOverridesChangedDTO(@JsonProperty("applications") Set<String> applications) {
        this.applications = applications;
    }

    public Set<String> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "I18nOverridesChangedDTO{applications=" + applications + '}';
    }
}
