package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ProductDTO;

import java.util.List;

public interface ProductDAO {
	
	public void create(ProductDTO dto) throws EntityCreationException, EntityRetrievalException;

	public void update(ProductDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<ProductDTO> findAll();
	
	public ProductDTO getById(Long id) throws EntityRetrievalException;
	
	public List<ProductDTO> getByVendor(Long vendorId);
	
	public List<ProductDTO> getByVendors(List<Long> vendorIds);
}
