package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductClassificationTypeDTO;

public interface ProductClassificationTypeDAO {

    void create(ProductClassificationTypeDTO dto) throws EntityCreationException, EntityRetrievalException;

    void update(ProductClassificationTypeDTO dto) throws EntityRetrievalException;

    void delete(Long id);

    List<ProductClassificationTypeDTO> findAll();

    ProductClassificationTypeDTO getById(Long id) throws EntityRetrievalException;

    ProductClassificationTypeDTO getByName(String name);

}
