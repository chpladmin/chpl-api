package gov.healthit.chpl.manager;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserPermissionsManager {
    void addPermission(CertificationBodyDTO acb, Long userId) throws EntityRetrievalException, UserRetrievalException;

    void deletePermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException;

    void deleteAllPermissionsForUser(final Long userId) throws EntityRetrievalException;
}
