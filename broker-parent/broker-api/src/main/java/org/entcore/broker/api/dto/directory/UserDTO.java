package org.entcore.broker.api.dto.directory;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for user information including basic profile and function data.
 */
public class UserDTO {
    private final String id;
    private final String displayName;
    private final String login;
    private final String profile;
    private final Map<String, List<String>> functions;

    @JsonCreator
    public UserDTO(
            @JsonProperty("id") String id,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("login") String login,
            @JsonProperty("profile") String profile,
            @JsonProperty("functions") Map<String, List<String>> functions) {
        this.id = id;
        this.displayName = displayName;
        this.login = login;
        this.profile = profile;
        this.functions = functions;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogin() {
        return login;
    }

    public String getProfile() {
        return profile;
    }

    public Map<String, List<String>> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", login='" + login + '\'' +
                ", profile='" + profile + '\'' +
                ", functions=" + functions +
                '}';
    }
}