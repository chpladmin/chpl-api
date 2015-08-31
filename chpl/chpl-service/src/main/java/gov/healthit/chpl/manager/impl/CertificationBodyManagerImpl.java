package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificationBodyManagerImpl extends ApplicationObjectSupport implements CertificationBodyManager {

	@Autowired
	private CertificationBodyDAO certificationBodyDAO;
	
	@Autowired UserDAO userDAO;
	
	@Autowired
	private MutableAclService mutableAclService;


	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public CertificationBodyDTO create(CertificationBodyDTO acb) throws EntityCreationException, EntityRetrievalException {
		// Create the ACB itself
		CertificationBodyDTO result = certificationBodyDAO.create(acb);

		// Grant the current principal administrative permission to the ACB
		addPermission(acb, new PrincipalSid(gov.healthit.chpl.auth.Util.getUsername()),
				BasePermission.ADMINISTRATION);
		
		logger.debug("Created acb " + acb
					+ " and granted admin permission to recipient " + gov.healthit.chpl.auth.Util.getUsername());
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public CertificationBodyDTO update(CertificationBodyDTO acb) throws EntityRetrievalException {
		CertificationBodyDTO result = certificationBodyDAO.update(acb);
		logger.debug("Updated acb " + acb);
		return result;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, 'delete') or hasPermission(#acb, admin)")
	public void delete(CertificationBodyDTO acb) {
		
		certificationBodyDAO.delete(acb.getId());
		// Delete the ACL information as well
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		mutableAclService.deleteAcl(oid, false);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Deleted acb " + acb + " including ACL permissions");
		}
		
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin) or hasPermission(#acb, read)")
	public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<String> userNames = new ArrayList<String>();
		List<AccessControlEntry> entries = acl.getEntries();
		for (int i = 0; i < entries.size(); i++) {
			Sid sid = entries.get(i).getSid();
			userNames.add(sid.toString());
		}

		//pull back the userdto for the sids
		List<UserDTO> users = userDAO.findByNames(userNames);
		return users;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public void addPermission(CertificationBodyDTO acb, Sid recipient, Permission permission) {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());

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

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public void deletePermission(CertificationBodyDTO acb, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getSid().equals(recipient)
					&& entries.get(i).getPermission().equals(permission)) {
				acl.deleteAce(i);
			}
		}

		mutableAclService.updateAcl(acl);
		logger.debug("Deleted acb " + acb + " ACL permission " + permission + " for recipient " + recipient);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#acb, admin)")
	public void deleteAllPermissionsOnAcb(CertificationBodyDTO acb, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(CertificationBodyDTO.class, acb.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if(entries.get(i).getSid().equals(recipient)) {
				acl.deleteAce(i);
			}
		}

		mutableAclService.updateAcl(acl);
		logger.debug("Deleted all acb " + acb + " ACL permissions for recipient " + recipient);
	}
	
	@Transactional(readOnly = true)
	@PostFilter("hasRole('ROLE_ADMIN') or hasPermission(filterObject, 'read') or hasPermission(filterObject, admin)")
	public List<CertificationBodyDTO> getAll() {
		
		return certificationBodyDAO.findAll();
		
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', read) or "
			+ "hasPermission(#id, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
	public CertificationBodyDTO getById(Long id) throws EntityRetrievalException {
		if (logger.isDebugEnabled()) {
			logger.debug("Returning acb with id: " + id);
		}

		return certificationBodyDAO.getById(id);
	}
	
	

	public MutableAclService getMutableAclService() {
		return mutableAclService;
	}
	
	public void setCertificationBodyDAO(CertificationBodyDAO acbDAO) {
		this.certificationBodyDAO = acbDAO;
	}

	public void setMutableAclService(MutableAclService mutableAclService) {
		this.mutableAclService = mutableAclService;
	}
	
}
