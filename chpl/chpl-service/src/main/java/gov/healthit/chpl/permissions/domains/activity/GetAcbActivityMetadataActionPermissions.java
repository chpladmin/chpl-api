package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetAcbActivityMetadataActionPermissions")
public class GetAcbActivityMetadataActionPermissions extends ActionPermissions {

    @Autowired
    public GetAcbActivityMetadataActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof CertificationBodyActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            CertificationBodyActivityMetadata metadata = (CertificationBodyActivityMetadata) obj;
            return isAcbValidForCurrentUser(metadata.getAcbId());
        }
    }
}
