package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.ProductVersionEntity;

public interface ProductVersionDAO {

    ProductVersionDTO create(ProductVersionDTO dto) throws EntityCreationException, EntityRetrievalException;

    ProductVersionEntity update(ProductVersionDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<ProductVersionDTO> findAll();

    ProductVersionDTO getById(Long id) throws EntityRetrievalException;

    List<ProductVersionDTO> getByProductId(Long productId);

    List<ProductVersionDTO> getByProductIds(List<Long> productId);

    ProductVersionDTO getByProductAndVersion(Long productId, String version);

}
