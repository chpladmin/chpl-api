package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ProductManager;

@Service
public class ProductManagerImpl implements ProductManager {

	@Autowired ProductDAO productDao;
	@Autowired ProductVersionDAO versionDao;
	
	@Autowired
	ActivityManager activityManager;
	
	@Override
	@Transactional(readOnly = true)
	public ProductDTO getById(Long id) throws EntityRetrievalException {
		return productDao.getById(id);
	}

	@Override
	@Transactional(readOnly = true) 
	public List<ProductDTO> getAll() {
		return productDao.findAll();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getByDeveloper(Long developerId) {
		return productDao.getByDeveloper(developerId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getByDevelopers(List<Long> developerIds) {
		return productDao.getByDevelopers(developerIds);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO result = productDao.create(dto);
		
		String activityMsg = "Product "+dto.getName()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, null, result);
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO beforeDTO = productDao.getById(dto.getId());
		
		ProductEntity result = productDao.update(dto);
		ProductDTO afterDto = new ProductDTO(result);
		
		String activityMsg = "Product "+dto.getName()+" was updated.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), activityMsg, beforeDTO, afterDto);
		return new ProductDTO(result);
		
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		delete(dto.getId());
		String activityMsg = "Product "+dto.getName()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, dto.getId(), activityMsg, dto, null);
	
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN') or hasRole('ROLE_ACB_STAFF')")
	public void delete(Long productId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO toDelete = productDao.getById(productId);
		String activityMsg = "Product "+ toDelete.getName() +" was deleted.";

		productDao.delete(productId);
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, productId, activityMsg, toDelete , null);
		
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ProductDTO merge(List<Long> productIdsToMerge, ProductDTO toCreate) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		List<ProductDTO> beforeProducts = new ArrayList<ProductDTO>();
		for(Long productId : productIdsToMerge) {
			beforeProducts.add(productDao.getById(productId));
		}
		
		ProductDTO createdProduct = productDao.create(toCreate);

		//search for any versions assigned to the list of products passed in
		List<ProductVersionDTO> assignedVersions = versionDao.getByProductIds(productIdsToMerge);
		//reassign those versions to the new product
		for(ProductVersionDTO version : assignedVersions) {
			version.setProductId(createdProduct.getId());
			versionDao.update(version);
		}
		
		// - mark the passed in products as deleted
		for(Long productId : productIdsToMerge) {
			productDao.delete(productId);
		}

		String activityMsg = "Merged "+ productIdsToMerge.size() + " products into new product '" + createdProduct.getName() + "'.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, createdProduct.getId(), activityMsg, beforeProducts , createdProduct);
		
		return createdProduct;
	}
	
}
