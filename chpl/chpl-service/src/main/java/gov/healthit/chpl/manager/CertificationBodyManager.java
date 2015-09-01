package gov.healthit.chpl.manager;


import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface CertificationBodyManager {
	
	
	public void addPermission(CertificationBodyDTO acb, Sid recipient, Permission permission);
	
	
	public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission);
	public void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient);
	
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws EntityCreationException, EntityRetrievalException;
	
	
	public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException;
	
	
	public void delete(CertificationBodyDTO acb);
	
	
	public List<CertificationBodyDTO> getAll();
	

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
	public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb);
	public List<Permission> getPermissionsForUser(CertificationBodyDTO acb, Sid recipient);
}
