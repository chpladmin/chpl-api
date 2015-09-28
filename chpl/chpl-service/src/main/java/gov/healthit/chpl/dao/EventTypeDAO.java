package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.EventTypeDTO;

public interface EventTypeDAO {
	public EventTypeDTO getById(Long id) throws EntityRetrievalException;
	public List<EventTypeDTO> getAll();
	public EventTypeDTO getByName(String eventName);
}
