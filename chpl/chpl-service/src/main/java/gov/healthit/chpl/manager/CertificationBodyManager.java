package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;

public interface CertificationBodyManager {

    void addPermission(CertificationBodyDTO acb, Long userId, Permission permission)
            throws UserRetrievalException;

    void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission);

    void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient);

    void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;

    CertificationBodyDTO create(CertificationBodyDTO acb)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException,
            JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException;

    CertificationBodyDTO retire(Long acbId) throws EntityRetrievalException,
        JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException;

    CertificationBodyDTO unretire(Long acbId) throws EntityRetrievalException,
    JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException;

    List<CertificationBodyDTO> getAllForUser();

    List<CertificationBodyDTO> getAll();
    List<CertificationBodyDTO> getAllActive();

    CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
    CertificationBodyDTO getIfPermissionById(final Long id) throws EntityRetrievalException;
    List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb);

    List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient);
}
