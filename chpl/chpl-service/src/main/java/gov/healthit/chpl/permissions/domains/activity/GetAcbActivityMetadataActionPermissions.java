package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetAcbActivityMetadataActionPermissions")
public class GetAcbActivityMetadataActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetAcbActivityMetadataActionPermissions.class);


    public GetAcbActivityMetadataActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof CertificationBodyActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            //Admin and onc can see all acb activity
            //including for retired acbs.
            //Acb user can see activity for their own acb.
            CertificationBodyActivityMetadata metadata = (CertificationBodyActivityMetadata) obj;
            return isAcbValidForCurrentUser(metadata.getAcbId());
        }
    }
}
