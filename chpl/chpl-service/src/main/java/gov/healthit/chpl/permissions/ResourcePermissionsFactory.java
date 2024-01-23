package gov.healthit.chpl.permissions;

import org.springframework.stereotype.Component;

@Component
public class ResourcePermissionsFactory {
    private ChplResourcePermissions chplResourcePermissions;

    public ResourcePermissionsFactory(ChplResourcePermissions chplResourcePermissions) {
        this.chplResourcePermissions = chplResourcePermissions;
    }

    public ResourcePermissions get() {
        return chplResourcePermissions;
    }
}
