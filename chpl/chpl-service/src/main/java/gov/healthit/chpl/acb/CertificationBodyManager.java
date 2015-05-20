package gov.healthit.chpl.acb;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


public interface CertificationBodyManager {
	
	
	public void addPermission(CertificationBody acb, Sid recipient, Permission permission);
	
	
	public void deletePermission(CertificationBody acb, Sid recipient, Permission permission);
	
	
	public void create(CertificationBody acb);
	
	
	public void update(CertificationBody acb);
	
	
	public void delete(CertificationBody acb);
	
	
	public List<CertificationBody> getAll();
	

	public CertificationBody getById(Long id);
	
}
