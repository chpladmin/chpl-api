package gov.healthit.chpl.acb;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;

@Component
public interface CertificationBodyManager {
	
	@PreAuthorize("hasPermission(#acb, admin)")
	public void addPermission(CertificationBody acb, Sid recipient, Permission permission);
	
	@PreAuthorize("hasPermission(#acb, admin)")
	public void deletePermission(CertificationBody acb, Sid recipient, Permission permission);
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void create(CertificationBody acb);
	
	@PreAuthorize("hasPermission(#acb, 'delete') or hasPermission(#acb, admin)")
	public void delete(CertificationBody acb);
	
	@PostFilter("hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<CertificationBody> getAll();
	
	@PreAuthorize("hasPermission(#id, 'gov.healthit.chpl.CertificationBody', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.CertificationBody', admin)")
	public CertificationBody getById(Long id);
	
}
