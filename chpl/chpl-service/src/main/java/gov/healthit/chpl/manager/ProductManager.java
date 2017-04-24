package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;

public interface ProductManager {
	public ProductDTO getById(Long id) throws EntityRetrievalException;
	public List<ProductDTO> getAll();
	public List<ProductDTO> getByDeveloper(Long developerId);
	public List<ProductDTO> getByDevelopers(List<Long> developerIds);
	public ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public ProductDTO update(ProductDTO dto, boolean lookForSuspiciousActivity) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public ProductDTO merge(List<Long> productIdsToMerge, ProductDTO toCreate) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public ProductDTO split(ProductDTO oldProduct, ProductDTO newProduct, String newProductCode, List<ProductVersionDTO> newProductVersions) throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void checkSuspiciousActivity(ProductDTO original, ProductDTO changed);
}
