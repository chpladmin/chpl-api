package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;

public interface ProductDAO {

    ProductDTO create(ProductDTO dto) throws EntityCreationException, EntityRetrievalException;

    ProductOwnerDTO addOwnershipHistory(ProductOwnerDTO toAdd);

    void deletePreviousOwner(Long previousOwnershipId) throws EntityRetrievalException;

    ProductDTO update(ProductDTO dto) throws EntityRetrievalException, EntityCreationException;

    void delete(Long id) throws EntityRetrievalException;

    List<ProductDTO> findAll();

    List<ProductDTO> findAllIncludingDeleted();

    ProductDTO getById(Long id) throws EntityRetrievalException;

    List<ProductDTO> getByDeveloper(Long vendorId);

    List<ProductDTO> getByDevelopers(List<Long> vendorIds);

    ProductDTO getByDeveloperAndName(Long vendorId, String name);
}
