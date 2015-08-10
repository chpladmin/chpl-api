package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ProductVersionDTO;

import java.util.List;

public interface ProductVersionDAO {
	
	public void create(ProductVersionDTO dto) throws EntityCreationException, EntityRetrievalException;

	public void update(ProductVersionDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<ProductVersionDTO> findAll();
	
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException;
	
}
