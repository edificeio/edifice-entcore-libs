package org.entcore.common.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisibleIdentityRequest {


    private String userId;
    private boolean itSelf;
    private boolean includeHiddenCommunity;
    private List<String> expectedVisiblesIds;
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
     * Restrict the response to those user or group ids. Null or empty means no restriction : every visible
     * of the user is returned.
     * @return
     */
    public List<String> getExpectedVisiblesIds() {
        return expectedVisiblesIds;
    }

    public VisibleIdentityRequest setExpectedVisiblesIds(List<String> expectedVisiblesIds) {
        this.expectedVisiblesIds = expectedVisiblesIds;
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
