package gov.healthit.chpl.manager.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandler;
import gov.healthit.chpl.certifiedProduct.upload.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDao;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class PendingCertifiedProductManagerImpl extends ApplicationObjectSupport implements PendingCertifiedProductManager {

	@Autowired CertifiedProductUploadHandlerFactory uploadHandlerFactory;
	@Autowired PendingCertifiedProductDao pcpDao;
	@Autowired CQMCriterionDAO cqmCriterionDAO;
	@Autowired CertificationBodyManager acbManager;
	@Autowired UserManager userManager;
	@Autowired UserDAO userDAO;
	@Autowired private MutableAclService mutableAclService;
	
	@Transactional
	
	@PreAuthorize("hasRole('ROLE_ACB_STAFF') or hasRole('ROLE_ACB_ADMIN')")
	//this also is restricted to users with permissions on the acb, but the acb is buried inside the file
	//so we have to check that manually before inserting rather than out here
	@Override
	public List<PendingCertifiedProductDetails> upload(MultipartFile file) 
		throws InvalidArgumentsException, EntityRetrievalException, IOException {
		
		List<PendingCertifiedProductDetails> results = new ArrayList<PendingCertifiedProductDetails>();
		
		BufferedReader reader = null;
		CSVParser parser = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			parser = new CSVParser(reader, CSVFormat.EXCEL);
			
			List<CSVRecord> records = parser.getRecords();
			if(records.size() <= 1) {
				throw new InvalidArgumentsException("The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
			}
			
			CSVRecord heading = records.get(0);
			for(int i = 1; i < records.size(); i++) {
				CSVRecord record = records.get(i);
				
				//some rows may be blank, we just look at the first column to see if it's empty or not
				if(!StringUtils.isEmpty(record.get(0))) {
					CertifiedProductUploadHandler handler = uploadHandlerFactory.getHandler(heading, record);
				
					//create a certified product to pass into the handler
					try {
						PendingCertifiedProductEntity pendingCp = handler.handle();
						if(pendingCp.getCertificationBodyId() == null) {
							throw new IllegalArgumentException("Could not find certifying body with name " + pendingCp.getCertificationBodyName() + ". Aborting upload.");
						}
						CertificationBodyDTO acb = acbManager.getById(pendingCp.getCertificationBodyId());
						if(acb == null || acb.getId() == null || acb.getId() < 0) {
							throw new IllegalArgumentException("No certifying body " + pendingCp.getCertificationBodyName() + " found for current user.");
						}

						//insert the record
						PendingCertifiedProductDTO pendingCpDto = pcpDao.create(pendingCp);
						//add appropriate ACLs
						//who already has access to this ACB?
						List<UserDTO> usersOnAcb = acbManager.getAllUsersOnAcb(acb);
						//give each of those people access to this PendingCertifiedProduct
						for(UserDTO user : usersOnAcb) {
							addPermission(acb, pendingCpDto, user, BasePermission.ADMINISTRATION);
						}
						
						PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCpDto);
						//set applicable criteria
						List<CQMCriterion> cqmCriteria = loadCQMCriteria();
						details.setApplicableCqmCriteria(handler.getApplicableCqmCriterion(cqmCriteria));
						results.add(details);
					} catch(EntityCreationException ex) {
						logger.error("could not create entity at row " + i + ". Message is " + ex.getMessage());
					}
				}
			}
		} catch(IOException ioEx) {
			logger.error("Could not get input stream for uploaded file " + file.getName());
			throw new IOException("Could not get input stream for uploaded file " + file.getName());
		} finally {
			 try { parser.close(); } catch(Exception ignore) {}
			try { reader.close(); } catch(Exception ignore) {}
		}
		return results;
	}

	@Override
	public void delete(PendingCertifiedProductDTO product) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and (hasPermission(filterObject, 'read') or hasPermission(filterObject, admin))")
	public List<PendingCertifiedProductDTO> getAll() {
		return pcpDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	@PostFilter("(hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')) and (hasPermission(filterObject, 'read') or hasPermission(filterObject, admin))")
	public PendingCertifiedProductDTO getById(Long id) throws EntityRetrievalException {
		return pcpDao.findById(id);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#acb, admin))")
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, Long userId, Permission permission) throws UserRetrievalException {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		UserDTO user = userDAO.getById(userId);
		if(user == null || user.getSubjectName() == null) {
			throw new UserRetrievalException("Could not find user with id " + userId);
		}
		
		Sid recipient = new PrincipalSid(user.getSubjectName());
		if(permissionExists(acl, recipient, permission)) {
			logger.debug("User " + recipient + " already has permission on the pending certified product " + pcpDto.getId());
		} else {
			acl.insertAce(acl.getEntries().size(), permission, recipient, true);
			mutableAclService.updateAcl(acl);
			logger.debug("Added permission " + permission + " for Sid " + recipient
					+ " pending certified product " + pcpDto.getId());
		}
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#acb, admin))")
	public void addPermission(CertificationBodyDTO acb, PendingCertifiedProductDTO pcpDto, UserDTO user, Permission permission) {
		MutableAcl acl;
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());

		try {
			acl = (MutableAcl) mutableAclService.readAclById(oid);
		}
		catch (NotFoundException nfe) {
			acl = mutableAclService.createAcl(oid);
		}

		UserDTO foundUser = userDAO.findUser(user);
		if(foundUser == null || foundUser.getId() == null) {
			//TODO: what to do about the password??
			//foundUser = userDAO.create(user, encodedPassword);
		}
		
		Sid recipient = new PrincipalSid(foundUser.getSubjectName());
		if(permissionExists(acl, recipient, permission)) {
			logger.debug("User " + recipient + " already has permission on the pending certified product " + pcpDto.getId());
		} else {
			acl.insertAce(acl.getEntries().size(), permission, recipient, true);
			mutableAclService.updateAcl(acl);
			logger.debug("Added permission " + permission + " for Sid " + recipient
					+ " pending certified product " + pcpDto);
		}
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#pcpDto, admin))")
	public void deletePermission(PendingCertifiedProductDTO pcpDto, Sid recipient, Permission permission) {
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		List<AccessControlEntry> entries = acl.getEntries();

		//if the current size is only 1 we shouldn't be able to delete the last one right??
		//then nobody would be able to ever add or delete or read from the pcp again
		if(entries != null && entries.size() > 1) {
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).getSid().equals(recipient)
						&& entries.get(i).getPermission().equals(permission)) {
					acl.deleteAce(i);
				}
			}
			mutableAclService.updateAcl(acl);
		}
		logger.debug("Deleted pcp " + pcpDto.getId() + " ACL permission " + permission + " for recipient " + recipient);
	}

	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB_ADMIN') and hasPermission(#pcpDto, admin))")
	public void deleteAllPermissionsOnPendingCertifiedProduct(PendingCertifiedProductDTO pcpDto, Sid recipient) {
		ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, pcpDto.getId());
		MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
		
		//TODO: this seems very dangerous. I think we should somehow prevent from deleting the ADMIN user???
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			if(entries.get(i).getSid().equals(recipient)) {
				acl.deleteAce(i);
				//cannot just loop through deleting because the "entries" 
				//list changes size each time that we delete one
				//so we have to re-fetch the entries and re-set the counter
				entries = acl.getEntries();
				i = 0;
			}
		}

		mutableAclService.updateAcl(acl);
		logger.debug("Deleted all pcp " + pcpDto.getId() + " ACL permissions for recipient " + recipient);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN')") 
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException {
		if(userDto.getSubjectName() == null) {
			userDto = userDAO.getById(userDto.getId());
		}
		
		List<PendingCertifiedProductDTO> dtos = pcpDao.findAll();
		for(PendingCertifiedProductDTO dto : dtos) {
			ObjectIdentity oid = new ObjectIdentityImpl(PendingCertifiedProductDTO.class, dto.getId());
			MutableAcl acl = (MutableAcl) mutableAclService.readAclById(oid);
			
			List<Permission> permissions = new ArrayList<Permission>();
			List<AccessControlEntry> entries = acl.getEntries();
			for (int i = 0; i < entries.size(); i++) {
				AccessControlEntry currEntry = entries.get(i);
				if(currEntry.getSid().equals(userDto.getSubjectName())) {
					permissions.remove(currEntry.getPermission());
				}
			}
		}
	}
	
	private boolean permissionExists(MutableAcl acl, Sid recipient, Permission permission) {
		boolean permissionExists = false;
		List<AccessControlEntry> entries = acl.getEntries();

		for (int i = 0; i < entries.size(); i++) {
			AccessControlEntry currEntry = entries.get(i);
			if(currEntry.getSid().equals(recipient) && 
					currEntry.getPermission().equals(permission)) {
				permissionExists = true;
			}
		}
		return permissionExists;
	}
	
	private List<CQMCriterion> loadCQMCriteria() {
		List<CQMCriterion> result = new ArrayList<CQMCriterion>();
		
		List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
		for (CQMCriterionDTO dto: dtos) {
			CQMCriterion criterion = new CQMCriterion();
			criterion.setCmsId(dto.getCmsId());
			criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			criterion.setCqmDomain(dto.getCqmDomain());
			criterion.setCqmVersionId(dto.getCqmVersionId());
			criterion.setCqmVersion(dto.getCqmVersion());
			criterion.setCriterionId(dto.getId());
			criterion.setDescription(dto.getDescription());
			criterion.setNqfNumber(dto.getNqfNumber());
			criterion.setNumber(dto.getNumber());
			criterion.setTitle(dto.getTitle());
			result.add(criterion);
		}
		
		return result;
	}
}
