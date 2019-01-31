package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserPermissionsManager {
    void addPermission(CertificationBodyDTO acb, Long userId) throws EntityRetrievalException;

    void deletePermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException;

    List<CertificationBodyDTO> getAllAcbsForCurrentUser();
}
