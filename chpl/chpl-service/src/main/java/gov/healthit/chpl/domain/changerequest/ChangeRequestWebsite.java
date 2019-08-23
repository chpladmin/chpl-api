package gov.healthit.chpl.domain.changerequest;

import java.io.Serializable;

public class ChangeRequestWebsite extends ChangeRequest implements Serializable {
    private static final long serialVersionUID = -5572794875424284955L;

    private String website;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
