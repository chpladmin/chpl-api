package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetAtlActivityMetadataActionPermissions")
public class GetAtlActivityMetadataActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetAtlActivityMetadataActionPermissions.class);


    public GetAtlActivityMetadataActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof TestingLabActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            //Admin and onc can see all atl activity
            //including for retired atls.
            //Atl user can see activity for their own atl.
            TestingLabActivityMetadata metadata = (TestingLabActivityMetadata) obj;
            return isAtlValidForCurrentUser(metadata.getAtlId());
        }
    }
}
