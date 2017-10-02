package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ProductVersionDTO;

public interface ProductVersionManager {
    public ProductVersionDTO getById(Long id) throws EntityRetrievalException;

    public List<ProductVersionDTO> getAll();

    public List<ProductVersionDTO> getByProduct(Long productId);

    public List<ProductVersionDTO> getByProducts(List<Long> productIds);

    public ProductVersionDTO create(ProductVersionDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    public ProductVersionDTO update(ProductVersionDTO dto)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    public ProductVersionDTO merge(List<Long> versionIdsToMerge, ProductVersionDTO toCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
}
