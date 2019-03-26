package gov.healthit.chpl.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.permissions.Permissions;

public abstract class SecuredManager {
    @Autowired
    protected Permissions permissions;
}
