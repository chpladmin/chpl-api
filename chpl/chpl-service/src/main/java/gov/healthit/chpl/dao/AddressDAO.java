package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface AddressDAO {

    AddressEntity create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException;

    AddressEntity update(AddressDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<AddressDTO> findAll();

    AddressDTO getById(Long id) throws EntityRetrievalException;

    AddressEntity getEntityById(Long id) throws EntityRetrievalException;

    // AddressDTO getByValues(AddressDTO address);

    AddressEntity saveAddress(AddressDTO addressDto) throws EntityRetrievalException, EntityCreationException;
}
