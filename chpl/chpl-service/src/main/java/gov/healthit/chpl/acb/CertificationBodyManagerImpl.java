package gov.healthit.chpl.acb;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;


public class CertificationBodyManagerImpl extends ApplicationObjectSupport implements CertificationBodyManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	private MutableAclService mutableAclService;
	
	private static int counter = 1000;
	
	@Transactional
	public void addPermission(CertificationBody acb, Sid recipient, Permission permission) {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBody.class, acb.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		acl.insertAce(acl.getEntries().size(), permission, recipient, true);
		mutableAclService.updateAcl(acl);

		logger.debug("Added permission " + permission + " for Sid " + recipient
				+ " acb " + acb);
	}
	
	public MutableAclService getMutableAclService() {
		return mutableAclService;
	}

	@Transactional
	public void create(CertificationBody acb) {
		// Create the ACB itself
		acb.setId(new Long(counter++));
		certificationBodyDAO.create(acb);

		// Grant the current principal administrative permission to the ACB
		addPermission(acb, new PrincipalSid(getUsername()),
				BasePermission.ADMINISTRATION);

		if (logger.isDebugEnabled()) {
			logger.debug("Created acb " + acb
					+ " and granted admin permission to recipient " + getUsername());
		}
	}

	@Transactional
	public void delete(CertificationBody acb) {
		
		certificationBodyDAO.delete(acb.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBody.class, acb.getId());
		mutableAclService.deleteAcl(oid, false);

		if (logger.isDebugEnabled()) {
			logger.debug("Deleted acb " + acb + " including ACL permissions");
		}
		
	}

	@Transactional
	public void deletePermission(CertificationBody acb, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBody.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getSid().equals(recipient)
					&& entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}

		mutableAclService.updateAcl(acl);

		if (logger.isDebugEnabled()) {
			logger.debug("Deleted acb " + acb + " ACL permissions for recipient "
					+ recipient);
		}
	}

	@Transactional(readOnly = true)
	public List<CertificationBody> getAll() {
		logger.debug("Returning all acbs");

		return certificationBodyDAO.findAll();
	}

	@Transactional(readOnly = true)
	public CertificationBody getById(Long id) {
		if (logger.isDebugEnabled()) {
			logger.debug("Returning acb with id: " + id);
		}

		return certificationBodyDAO.getById(id);
	}
	
	@Transactional
	protected String getUsername() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth.getPrincipal() instanceof UserDetails) {
			return ((UserDetails) auth.getPrincipal()).getUsername();
		}
		else {
			return auth.getPrincipal().toString();
		}
	}
	
	public void setCertificationBodyDAO(CertificationBodyDAO acbDAO) {
		this.certificationBodyDAO = acbDAO;
	}

	public void setMutableAclService(MutableAclService mutableAclService) {
		this.mutableAclService = mutableAclService;
	}

	public void update(CertificationBody acb) {
		certificationBodyDAO.update(acb);
		logger.debug("Updated acb " + acb);
	}
}
