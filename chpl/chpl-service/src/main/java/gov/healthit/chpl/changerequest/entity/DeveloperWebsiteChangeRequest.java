package gov.healthit.chpl.changerequest.entity;

import gov.healthit.chpl.domain.Developer;

/**
 * This entity object is meant to be used by clients to send the information
 * required for creating website chnage request.
 * 
 * @author TYoung
 */
public class DeveloperWebsiteChangeRequest {
    private Developer developer;
    private String website;

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }
}
