package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ProductVersionManager;

@Service
public class ProductVersionManagerImpl implements ProductVersionManager {

	@Autowired ProductVersionDAO dao;
	
	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException {
		return dao.getById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVersionDTO> getAll() {
		return dao.findAll();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductVersionDTO> getByProduct(Long productId) {
		return dao.getByProductId(productId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductVersionDTO> getByProducts(List<Long> productIds) {
		return dao.getByProductIds(productIds);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductVersionDTO create(ProductVersionDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		ProductVersionDTO created = dao.create(dto);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, created.getId(), "Product Version "+dto.getVersion()+" added for product "+dto.getProductId(), null, created);
		return created;
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductVersionDTO update(ProductVersionDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		ProductVersionDTO before = dao.getById(dto.getId());
		ProductVersionEntity result = dao.update(dto);
		ProductVersionDTO after = new ProductVersionDTO(result);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, after.getId(), "Product Version "+dto.getVersion()+" updated for product "+dto.getProductId(), before, after);
		return after;
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(ProductVersionDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		delete(dto.getId());
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(Long id) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
		
		ProductVersionDTO toDelete = dao.getById(id);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_VERSION, toDelete.getId(), "Product Version "+toDelete.getVersion()+" deleted for product "+toDelete.getProductId(), toDelete, null);
		dao.delete(id);
	}
}