package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.domain.activity.ActivityEvent;

public class ProductActivityEvent extends ActivityEvent implements Serializable {
    private static final long serialVersionUID = 6724369230954969251L;
    private Developer developer;

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

}
