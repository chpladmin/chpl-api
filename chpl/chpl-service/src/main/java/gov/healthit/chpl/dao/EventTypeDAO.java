package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.EventTypeDTO;
import gov.healthit.chpl.entity.EventTypeEntity;

public interface EventTypeDAO {
	
	public EventTypeEntity create(EventTypeDTO dto) throws EntityCreationException, EntityRetrievalException;
	public EventTypeEntity update(EventTypeDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;
	public EventTypeDTO getById(Long id) throws EntityRetrievalException;
	public List<EventTypeDTO> findAll();
	
}