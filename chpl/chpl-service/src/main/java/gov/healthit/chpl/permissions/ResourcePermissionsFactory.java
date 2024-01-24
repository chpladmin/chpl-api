package gov.healthit.chpl.permissions;

import org.ff4j.FF4j;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;

@Component
public class ResourcePermissionsFactory {
    private ChplResourcePermissions chplResourcePermissions;
    private SsoResourcePermissions ssoResourcePermissions;
    private FF4j ff4j;

    public ResourcePermissionsFactory(ChplResourcePermissions chplResourcePermissions, SsoResourcePermissions ssoResourcePermissions, FF4j ff4j) {
        this.chplResourcePermissions = chplResourcePermissions;
        this.ssoResourcePermissions = ssoResourcePermissions;
        this.ff4j = ff4j;
    }

    public ResourcePermissions get() {
        if (ff4j.check(FeatureList.SSO)) {
            return ssoResourcePermissions;
        } else {
            return chplResourcePermissions;
        }
    }
}
