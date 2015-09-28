package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.EventTypeDAO;
import gov.healthit.chpl.dto.EventTypeDTO;
import gov.healthit.chpl.entity.EventTypeEntity;

@Repository("eventTypeDao")
public class EventTypeDAOImpl extends BaseDAOImpl implements EventTypeDAO {

	@Override
	public EventTypeDTO getById(Long id)
			throws EntityRetrievalException {
		
		EventTypeDTO dto = null;
		EventTypeEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new EventTypeDTO(entity);
		}
		return dto;
	}
	
	@Override
	public EventTypeDTO getByName(String name) {
		
		EventTypeEntity entity = getEntityByName(name);
		EventTypeDTO dto = new EventTypeDTO(entity);
		return dto;
		
	}
	
	@Override
	public List<EventTypeDTO> getAll() {
		
		List<EventTypeEntity> entities = getAllEntities();
		List<EventTypeDTO> dtos = new ArrayList<>();
		
		for (EventTypeEntity entity : entities) {
			EventTypeDTO dto = new EventTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private List<EventTypeEntity> getAllEntities() {
		
		List<EventTypeEntity> result = entityManager.createQuery( "from EventTypeEntity where (NOT deleted = true) ", EventTypeEntity.class).getResultList();
		return result;
		
	}
	
	private EventTypeEntity getEntityByName(String name) {
		EventTypeEntity entity = null;

		Query query = entityManager.createQuery( "from EventTypeEntity where (NOT deleted = true) and (name = :name)", EventTypeEntity.class);
		query.setParameter("name", name);
		List<EventTypeEntity> result = query.getResultList();
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private EventTypeEntity getEntityById(Long id) throws EntityRetrievalException {
		
		EventTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from EventTypeEntity where (NOT deleted = true) AND (event_type_id = :entityid) ", EventTypeEntity.class );
		query.setParameter("entityid", id);
		List<EventTypeEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}