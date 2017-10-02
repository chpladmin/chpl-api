package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;

public interface AddressDAO {

    public AddressEntity create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException;

    public AddressEntity update(AddressDTO dto) throws EntityRetrievalException;

    public void delete(Long id) throws EntityRetrievalException;

    public List<AddressDTO> findAll();

    public AddressDTO getById(Long id) throws EntityRetrievalException;

    public AddressEntity getEntityById(Long id) throws EntityRetrievalException;

    public AddressDTO getByValues(AddressDTO address);

    public AddressEntity mergeAddress(AddressDTO addressDto) throws EntityRetrievalException, EntityCreationException;
}
