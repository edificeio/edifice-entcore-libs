package org.entcore.common.user.dto;

import io.vertx.core.json.JsonObject;

public class VisibleIdentityRequest {


    private String userId;
    private boolean itSelf;
    private boolean includeHiddenCommunity;
    private JsonObject params;
    private boolean publicDetails;

    public String getUserId() {
        return userId;
    }

    /**
     * Id of the user doing the visible request
     * @param userId
     * @return
     */
    public VisibleIdentityRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Include himself in the response
     * @return
     */
    public boolean isItSelf() {
        return itSelf;
    }

    public VisibleIdentityRequest setItSelf(boolean itSelf) {
        this.itSelf = itSelf;
        return this;
    }

    /**
     * Include hidden group use by Communities
     * @return
     */
    public boolean isIncludeHiddenCommunity() {
        return includeHiddenCommunity;
    }

    public VisibleIdentityRequest setIncludeHiddenCommunity(boolean includeHiddenCommunity) {
        this.includeHiddenCommunity = includeHiddenCommunity;
        return this;
    }

    /**
     * Extra params, contains list of user or group as filter for example
     * @return
     */
    public JsonObject getParams() {
        return params;
    }

    public VisibleIdentityRequest setParams(JsonObject params) {
        this.params = params;
        return this;
    }

    /**
     * Return only id + type of visible, or add public general information (dislayName / name / groupName
     *  structureName, profile
     * @return
     */
    public boolean isPublicDetails() {
        return publicDetails;
    }

    public VisibleIdentityRequest setPublicDetails(boolean publicDetails) {
        this.publicDetails = publicDetails;
        return this;
    }
}
