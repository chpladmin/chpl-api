package gov.healthit.chpl.entity.lastmodifieduserstrategy;

import org.ff4j.FF4j;

import gov.healthit.chpl.SpringContext;
import gov.healthit.chpl.entity.EntityAudit;

public abstract class LastModifiedUserStrategy {
    private FF4j ff4j = SpringContext.getBean(FF4j.class);

    public abstract void populateLastModifiedUser(EntityAudit entityAudit);

    public FF4j getFF4j() {
        return ff4j;
    }
}
