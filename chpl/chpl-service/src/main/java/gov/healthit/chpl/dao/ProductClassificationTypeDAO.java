package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ProductClassificationTypeDTO;

public interface ProductClassificationTypeDAO {

    public void create(ProductClassificationTypeDTO dto) throws EntityCreationException, EntityRetrievalException;

    public void update(ProductClassificationTypeDTO dto) throws EntityRetrievalException;

    public void delete(Long id);

    public List<ProductClassificationTypeDTO> findAll();

    public ProductClassificationTypeDTO getById(Long id) throws EntityRetrievalException;

    public ProductClassificationTypeDTO getByName(String name);

}
