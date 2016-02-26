package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UcdProcessDTO;

public interface UcdProcessDAO {
	
	public UcdProcessDTO create(UcdProcessDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public UcdProcessDTO update(UcdProcessDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<UcdProcessDTO> findAll();
	public UcdProcessDTO getById(Long id) throws EntityRetrievalException;
	public UcdProcessDTO getByName(String name );
}
