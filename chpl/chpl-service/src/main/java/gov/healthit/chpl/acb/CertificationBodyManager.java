package gov.healthit.chpl.acb;

import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface CertificationBodyManager {
	
	
	public void addPermission(CertificationBodyEntity acb, Sid recipient, Permission permission);
	
	
	public void deletePermission(CertificationBodyEntity acb, Sid recipient, Permission permission);
	
	
	public void create(CertificationBodyEntity acb);
	
	
	public void update(CertificationBodyEntity acb);
	
	
	public void delete(CertificationBodyEntity acb);
	
	
	public List<CertificationBodyEntity> getAll();
	

	public CertificationBodyEntity getById(Long id);
	
}
