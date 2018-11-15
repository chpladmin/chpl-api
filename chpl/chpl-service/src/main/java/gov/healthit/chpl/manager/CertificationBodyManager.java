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

    public void addPermission(CertificationBodyDTO acb, Long userId, Permission permission)
            throws UserRetrievalException;

    public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission);

    public void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient);

    public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;

    public CertificationBodyDTO create(CertificationBodyDTO acb)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException,
            JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException;

    public List<CertificationBodyDTO> getAllForUser();

    public List<CertificationBodyDTO> getAll();
    public List<CertificationBodyDTO> getAllActive();

    public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;

    public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb);

    public List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient);
}
