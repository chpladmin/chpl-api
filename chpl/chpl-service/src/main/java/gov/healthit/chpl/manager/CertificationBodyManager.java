package gov.healthit.chpl.manager;


import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface CertificationBodyManager {
	
	public void addPermission(CertificationBodyDTO acb, Long userId, Permission permission) throws UserRetrievalException;
	
	public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission);
	public void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient);
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;
	
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws UserRetrievalException, EntityCreationException, EntityRetrievalException;
	
	
	public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException;
	
	
	public void delete(CertificationBodyDTO acb);
	
	
	public List<CertificationBodyDTO> getAllForUser();
	

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
	public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb);
	public List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient);
}
