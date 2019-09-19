package gov.healthit.chpl.changerequest.builders;

import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;

public class ChangeRequestWebsiteBuilder {

    private Long id;
    private String website;

    public ChangeRequestWebsiteBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ChangeRequestWebsiteBuilder withWebsite(String website) {
        this.website = website;
        return this;
    }

    public ChangeRequestWebsite build() {
        ChangeRequestWebsite crWebsite = new ChangeRequestWebsite();
        crWebsite.setId(id);
        crWebsite.setWebsite(website);
        return crWebsite;
    }
}
