package gov.healthit.chpl.domain.changerequest;

import java.io.Serializable;

public class ChangeRequestWebsite implements Serializable {
    private static final long serialVersionUID = -5572794875424284955L;

    private Long id;
    private String website;
    private ChangeRequest changeRequest;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public ChangeRequest getChangeRequest() {
        return changeRequest;
    }

    public void setChangeRequest(final ChangeRequest changeRequest) {
        this.changeRequest = changeRequest;
    }

}
