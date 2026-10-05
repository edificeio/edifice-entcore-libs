package org.entcore.broker.api.dto.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Translation overrides of the requested applications. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetI18nOverridesResponseDTO {

    private final boolean success;
    /** Cause of the failure, null on success */
    private final String errorMsg;
    private final List<ApplicationI18nOverridesDTO> applications;

    @JsonCreator
    public GetI18nOverridesResponseDTO(@JsonProperty("success") boolean success,
                                       @JsonProperty("errorMsg") String errorMsg,
                                       @JsonProperty("applications") List<ApplicationI18nOverridesDTO> applications) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.applications = applications;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<ApplicationI18nOverridesDTO> getApplications() {
        return applications;
    }
}
