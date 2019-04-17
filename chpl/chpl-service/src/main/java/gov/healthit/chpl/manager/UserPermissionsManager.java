package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface UserPermissionsManager {
    void addAcbPermission(CertificationBodyDTO acb, Long userId)
            throws EntityRetrievalException, UserRetrievalException;

    void deleteAcbPermission(CertificationBodyDTO acb, Long userId) throws EntityRetrievalException;

    void addAtlPermission(TestingLabDTO acb, Long userId) throws EntityRetrievalException, UserRetrievalException;

    void deleteAtlPermission(TestingLabDTO acb, Long userId) throws EntityRetrievalException;

}
