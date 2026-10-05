package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantI18nOverridesDTO {

    private final String tenantId;
    private final List<I18nOverrideValuesDTO> values;

    @JsonCreator
    public TenantI18nOverridesDTO(@JsonProperty("tenantId") String tenantId,
                                  @JsonProperty("values") List<I18nOverrideValuesDTO> values) {
        this.tenantId = tenantId;
        this.values = values;
    }

    public String getTenantId() {
        return tenantId;
    }

    public List<I18nOverrideValuesDTO> getValues() {
        return values;
    }
}
