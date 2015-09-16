package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;

import java.util.List;

public interface ProductVersionDAO {
	
	public ProductVersionEntity create(ProductVersionDTO dto) throws EntityCreationException, EntityRetrievalException;

	public ProductVersionEntity update(ProductVersionDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<ProductVersionDTO> findAll();
	
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException;
	
	public List<ProductVersionDTO> getByProductId(Long productId);
	
	public List<ProductVersionDTO> getByProductIds(List<Long> productId);
	
	public ProductVersionDTO getByProductAndVersion(Long productId, String version);
	
}
