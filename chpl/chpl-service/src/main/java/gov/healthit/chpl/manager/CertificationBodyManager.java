package gov.healthit.chpl.manager;


import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface CertificationBodyManager {
	
	
	public void addPermission(CertificationBodyDTO acb, Sid recipient, Permission permission);
	
	
	public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission);
	
	
	public void create(CertificationBodyDTO acb) throws EntityCreationException;
	
	
	public void update(CertificationBodyDTO acb) throws EntityRetrievalException;
	
	
	public void delete(CertificationBodyDTO acb);
	
	
	public List<CertificationBodyDTO> getAll();
	

	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException;
	
}
