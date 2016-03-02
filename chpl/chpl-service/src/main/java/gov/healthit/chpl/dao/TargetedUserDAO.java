package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TargetedUserDTO;

public interface TargetedUserDAO {
	
	public TargetedUserDTO create(TargetedUserDTO dto) throws EntityCreationException, EntityRetrievalException;	
	public TargetedUserDTO update(TargetedUserDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<TargetedUserDTO> findAll();
	public TargetedUserDTO getById(Long id) throws EntityRetrievalException;
	public TargetedUserDTO getByName(String name) ;
}
