package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.ProductEntity;

import java.util.List;

public interface ProductDAO {
	
	public ProductEntity create(ProductDTO dto) throws EntityCreationException, EntityRetrievalException;

	public ProductEntity update(ProductDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<ProductDTO> findAll();
	
	public ProductDTO getById(Long id) throws EntityRetrievalException;
	
	public List<ProductDTO> getByVendor(Long vendorId);
	
	public List<ProductDTO> getByVendors(List<Long> vendorIds);
	
	public ProductDTO getByVendorAndName(Long vendorId, String name);
}
