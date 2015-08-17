package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.ProductVersionManager;

@Service
public class ProductVersionManagerImpl implements ProductVersionManager {

	@Autowired ProductVersionDAO dao;
	
	@Override
	@Transactional(readOnly = true)
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException {
		return dao.getById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductVersionDTO> getByProduct(Long productId) {
		return dao.getByProductId(productId);
	}
	
	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void create(ProductVersionDTO dto) throws EntityRetrievalException, EntityCreationException {
		//TODO: something
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void update(ProductVersionDTO dto) throws EntityRetrievalException {
		//TODO: something
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(ProductVersionDTO dto) {
		delete(dto.getId());
	}

	@Override
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void delete(Long id) {
		dao.delete(id);
	}
}
