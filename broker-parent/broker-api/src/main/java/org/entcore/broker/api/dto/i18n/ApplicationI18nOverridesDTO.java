package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Translation overrides of an application. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationI18nOverridesDTO {

    /** The application, e.g. "workspace" */
    private final String application;
    /** Overrides applying to the users of each tenant (inherited from its parent tenants) */
    private final List<TenantI18nOverridesDTO> tenants;
    /**
     * Overrides applying to the requests made at each domain; "default-domain" holds those
     * applying to all domains
     */
    private final List<DomainI18nOverridesDTO> domains;

    @JsonCreator
    public ApplicationI18nOverridesDTO(@JsonProperty("application") String application,
                                       @JsonProperty("tenants") List<TenantI18nOverridesDTO> tenants,
                                       @JsonProperty("domains") List<DomainI18nOverridesDTO> domains) {
        this.application = application;
        this.tenants = tenants;
        this.domains = domains;
    }

    public String getApplication() {
        return application;
    }

    public List<TenantI18nOverridesDTO> getTenants() {
        return tenants;
    }

    public List<DomainI18nOverridesDTO> getDomains() {
        return domains;
    }
}
