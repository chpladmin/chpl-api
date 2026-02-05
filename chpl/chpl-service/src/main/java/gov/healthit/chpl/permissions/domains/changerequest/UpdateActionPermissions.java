package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {
    private ChangeRequestDAO changeRequestDao;

    @Autowired
    public UpdateActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao,
            ChangeRequestDAO changeRequestDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        this.changeRequestDao = changeRequestDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChangeRequestUpdateRequest)) {
                return false;
            } else if (getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAdmin()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                ChangeRequest cr = changeRequestDao.get((((ChangeRequestUpdateRequest) obj).getChangeRequest()).getId());
                return cr.getCertificationBodies().stream()
                        .map(acb -> acb.getId())
                        .filter(acbId -> isAcbValidForCurrentUser(acbId))
                        .findAny()
                        .isPresent();
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                ChangeRequest cr = changeRequestDao.get((((ChangeRequestUpdateRequest) obj).getChangeRequest()).getId());
                return isDeveloperValidForCurrentUser(cr.getDeveloper().getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
