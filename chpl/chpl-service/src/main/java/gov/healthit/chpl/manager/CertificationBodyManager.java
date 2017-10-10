package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
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

    void undelete(CertificationBodyDTO acb)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException;

    void delete(CertificationBodyDTO acb)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException;

    List<CertificationBodyDTO> getAllForUser(boolean showDeleted);

    List<CertificationBodyDTO> getAll(boolean showDeleted);

    CertificationBodyDTO getById(Long id) throws EntityRetrievalException;

    CertificationBodyDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;

    List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb);

    List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient);
}
