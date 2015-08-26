package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ProductVersionDTO;

public interface ProductVersionManager {
	public ProductVersionDTO getById(Long id) throws EntityRetrievalException;
	public List<ProductVersionDTO> getByProduct(Long productId);
	public List<ProductVersionDTO> getByProducts(List<Long> productIds);
	public ProductVersionDTO create(ProductVersionDTO dto) throws EntityRetrievalException, EntityCreationException;
	public ProductVersionDTO update(ProductVersionDTO dto) throws EntityRetrievalException;
	public void delete(ProductVersionDTO dto) throws EntityRetrievalException;
	public void delete(Long productVersionId) throws EntityRetrievalException;
}
