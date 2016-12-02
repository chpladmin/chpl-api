package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;

public interface ProductDAO {
	
	public ProductDTO create(ProductDTO dto) throws EntityCreationException, EntityRetrievalException;
	public ProductOwnerDTO addOwnershipHistory(ProductOwnerDTO toAdd);
	public void deletePreviousOwner(Long previousOwnershipId) throws EntityRetrievalException;
	public ProductDTO update(ProductDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<ProductDTO> findAll();
	
	public List<ProductDTO> findAllIncludingDeleted();
	
	public ProductDTO getById(Long id) throws EntityRetrievalException;
	
	public List<ProductDTO> getByDeveloper(Long vendorId);
	
	public List<ProductDTO> getByDevelopers(List<Long> vendorIds);
	
	public ProductDTO getByDeveloperAndName(Long vendorId, String name);
}
