package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ProductDTO;

public interface ProductManager {
	public ProductDTO getById(Long id) throws EntityRetrievalException;
	public List<ProductDTO> getAll();
	public List<ProductDTO> getByVendor(Long vendorId);
	public List<ProductDTO> getByVendors(List<Long> vendorIds);
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void delete(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void delete(Long productId) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
