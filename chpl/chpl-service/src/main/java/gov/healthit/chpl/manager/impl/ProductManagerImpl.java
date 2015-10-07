package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ProductManager;

@Service
public class ProductManagerImpl implements ProductManager {

	@Autowired ProductDAO productDao;
	
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
	public List<ProductDTO> getByVendor(Long vendorId) {
		return productDao.getByVendor(vendorId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductDTO> getByVendors(List<Long> vendorIds) {
		return productDao.getByVendors(vendorIds);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductEntity result = productDao.create(dto);
		String msg = "Product "+dto.getName()+" was created with ID "+result.getId();
		
		String afterJSON = JSONUtils.getWriter().writeValueAsString(dto);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), msg, null, afterJSON);
		return new ProductDTO(result);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductEntity result = productDao.update(dto);
		String msg = "Product "+dto.getName()+" was updated.";	
		ProductDTO afterDto = new ProductDTO(result);
		
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, result.getId(), msg, dto, afterDto);
		return new ProductDTO(result);
		
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
				
		delete(dto.getId());
		String msg = "Product "+dto.getName()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, dto.getId(), msg, dto, null);
	
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(Long productId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
		
		ProductDTO toDelete = productDao.getById(productId);
		productDao.delete(productId);
		
		String msg = "Product "+productId.toString()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PRODUCT, productId, msg, toDelete , null);
		
	}
	
}
