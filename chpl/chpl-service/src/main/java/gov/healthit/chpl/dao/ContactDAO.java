package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.ContactEntity;

public interface ContactDAO {

    ContactEntity create(ContactDTO dto) throws EntityCreationException, EntityRetrievalException;

    ContactEntity update(ContactDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<ContactDTO> findAll();

    ContactDTO getById(Long id) throws EntityRetrievalException;

    ContactEntity getEntityById(Long id) throws EntityRetrievalException;

    ContactDTO getByValues(ContactDTO dto);
}
