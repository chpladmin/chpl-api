package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.activity.ActivityDetails;

public class ProductActivityEvent extends ActivityDetails implements Serializable {
    private static final long serialVersionUID = 6724369230954969251L;
    private Developer developer;

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

}
