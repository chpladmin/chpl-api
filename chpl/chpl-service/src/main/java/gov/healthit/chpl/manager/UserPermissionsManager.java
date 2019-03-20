package gov.healthit.chpl.manager;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserPermissionsManager {
    void addAcbPermission(CertificationBodyDTO acb, Long userId)
            throws EntityRetrievalException, UserRetrievalException;

    void deleteAcbPermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException;

    void deleteAllAcbPermissionsForUser(final Long userId) throws EntityRetrievalException;

    void addAtlPermission(TestingLabDTO acb, Long userId) throws EntityRetrievalException, UserRetrievalException;

    void deleteAtlPermission(final TestingLabDTO acb, final Long userId) throws EntityRetrievalException;

    void deleteAllAtlPermissionsForUser(final Long userId) throws EntityRetrievalException;

}
