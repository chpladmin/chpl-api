package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserPermissionsManager {
    void addPermission(CertificationBodyDTO acb, Long userId) throws EntityRetrievalException, UserRetrievalException;

    void deletePermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException;

    List<CertificationBodyDTO> getAllAcbsForCurrentUser();

    List<UserDTO> getAllUsersOnAcb(final CertificationBodyDTO acb);
}
