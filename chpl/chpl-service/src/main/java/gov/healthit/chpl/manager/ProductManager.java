package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ProductManager {
    ProductDTO getById(Long id) throws EntityRetrievalException;
    ProductDTO getById(Long id, boolean allowDeleted) throws EntityRetrievalException;

    List<ProductDTO> getAll();

    List<ProductDTO> getByDeveloper(Long developerId);

    List<ProductDTO> getByDevelopers(List<Long> developerIds);

    ProductDTO create(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    ProductDTO updateProductOwnership(ProductDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    ProductDTO merge(List<Long> productIdsToMerge, ProductDTO toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    ProductDTO split(ProductDTO oldProduct, ProductDTO newProduct, String newProductCode,
            List<ProductVersionDTO> newProductVersions)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
