package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.manager.ProductManager;

@Service
public class ProductManagerImpl implements ProductManager {

	@Autowired ProductDAO productDao;
	
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
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException {
		ProductEntity result = productDao.create(dto);
		return new ProductDTO(result);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException {
		ProductEntity result = productDao.update(dto);
		return new ProductDTO(result);
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(ProductDTO dto) throws EntityRetrievalException {
		delete(dto.getId());
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(Long productId) throws EntityRetrievalException {
		productDao.delete(productId);
	}

	
}
