package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DomainI18nOverridesDTO {

    private final String domain;
    private final List<I18nOverrideValuesDTO> values;

    @JsonCreator
    public DomainI18nOverridesDTO(@JsonProperty("domain") String domain,
                                  @JsonProperty("values") List<I18nOverrideValuesDTO> values) {
        this.domain = domain;
        this.values = values;
    }

    public String getDomain() {
        return domain;
    }

    public List<I18nOverrideValuesDTO> getValues() {
        return values;
    }
}
