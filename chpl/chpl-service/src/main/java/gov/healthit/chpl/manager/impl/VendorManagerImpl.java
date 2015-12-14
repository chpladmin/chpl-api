package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.VendorDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.Vendor;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.VendorManager;

@Service
public class VendorManagerImpl implements VendorManager {

	@Autowired
	VendorDAO vendorDao;
	
	@Autowired ProductDAO productDao;
	
	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public List<VendorDTO> getAll() {
		return vendorDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public VendorDTO getById(Long id) throws EntityRetrievalException {
		return vendorDao.getById(id);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public VendorDTO update(VendorDTO vendor) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		VendorDTO before = vendorDao.getById(vendor.getId());
		VendorEntity result = vendorDao.update(vendor);
		VendorDTO after = new VendorDTO(result);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, after.getId(), "Vendor "+vendor.getName()+" was updated.", before, after);
		
		return new VendorDTO(result);
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public VendorDTO create(VendorDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		VendorDTO created = vendorDao.create(dto);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, created.getId(), "Vendor "+created.getName()+" has been created.", null, created);
		return created;
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public void delete(VendorDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		VendorDTO toDelete = vendorDao.getById(dto.getId());
		vendorDao.delete(dto.getId());
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, toDelete.getId(), "Vendor "+toDelete.getName()+" has been deleted.", toDelete, null);
		
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	@Transactional(readOnly = false)
	public void delete(Long vendorId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		VendorDTO toDelete = vendorDao.getById(vendorId);
		vendorDao.delete(vendorId);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, toDelete.getId(), "Vendor "+toDelete.getName()+" has been deleted.", toDelete, null);
	}
	
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional(readOnly = false)
	public VendorDTO merge(List<Long> vendorIdsToMerge, VendorDTO vendorToCreate) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		List<VendorDTO> beforeVendors = new ArrayList<VendorDTO>();
		for(Long vendorId : vendorIdsToMerge) {
			beforeVendors.add(vendorDao.getById(vendorId));
		}
		
		VendorDTO createdVendor = vendorDao.create(vendorToCreate);
		// - search for any products assigned to the list of vendors passed in
		List<ProductDTO> vendorProducts = productDao.getByVendors(vendorIdsToMerge);
		// - reassign those products to the new vendor
		for(ProductDTO product : vendorProducts) {
			product.setVendorId(createdVendor.getId());
			productDao.update(product);
		}
		// - mark the passed in vendors as deleted
		for(Long vendorId : vendorIdsToMerge) {
			vendorDao.delete(vendorId);
		}
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VENDOR, createdVendor.getId(), "Merged "+ vendorIdsToMerge.size() + " vendors into new vendor '" + createdVendor.getName() + "'.", beforeVendors, createdVendor);
		
		return createdVendor;
	}
}
