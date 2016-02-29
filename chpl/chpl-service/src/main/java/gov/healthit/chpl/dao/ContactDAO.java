package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.ContactEntity;

import java.util.List;

public interface ContactDAO {
	
	public ContactEntity create(ContactDTO dto) throws EntityCreationException, EntityRetrievalException;
	
	public ContactEntity update(ContactDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<ContactDTO> findAll();	
	public ContactDTO getById(Long id) throws EntityRetrievalException;
	public ContactEntity getEntityById(Long id) throws EntityRetrievalException;
	public ContactDTO getByValues(ContactDTO dto);
}
