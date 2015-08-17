package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.entity.AddressEntity;

import java.util.List;

public interface AddressDAO {
	
	public void create(AddressDTO dto) throws EntityCreationException, EntityRetrievalException;
	
	public void update(AddressDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<AddressDTO> findAll();
	public List<AddressEntity> findAllEntities();
	
	public AddressDTO getById(Long id) throws EntityRetrievalException;
	public AddressEntity getEntityById(Long id) throws EntityRetrievalException;
	
	public AddressDTO search(String line1, String line2, String city, String region, String country);
	public AddressEntity searchForEntity(String line1, String line2, String city, String region, String country);
}
