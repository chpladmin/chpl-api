package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;

import java.util.List;

public interface AddressDAO {
	
	public AddressEntity create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException;
	
	public AddressEntity update(AddressDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<AddressDTO> findAll();	
	public AddressDTO getById(Long id) throws EntityRetrievalException;
	public AddressDTO getByValues(AddressDTO address);
	public AddressEntity getEntityByValues(AddressDTO address);
}
