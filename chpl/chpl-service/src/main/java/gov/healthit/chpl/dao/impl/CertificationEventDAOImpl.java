package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.CertificationEventEntity;
import gov.healthit.chpl.entity.EventTypeEntity;
import gov.healthit.chpl.entity.VendorEntity;

@Repository("certificationEventDao")
public class CertificationEventDAOImpl extends BaseDAOImpl implements CertificationEventDAO {
	private static final Logger logger = LogManager.getLogger(CertificationEventDAOImpl.class);

	@Override
	public CertificationEventDTO create(CertificationEventDTO dto) throws EntityCreationException {
		CertificationEventEntity toCreate = new CertificationEventEntity();
		toCreate.setCertifiedProductId(dto.getCertifiedProductId());
		toCreate.setCity(dto.getCity());
		toCreate.setEventDate(dto.getEventDate());
		toCreate.setState(dto.getState());
		toCreate.setLastModifiedDate(new Date());
		toCreate.setLastModifiedUser(Util.getCurrentUser().getId());
		toCreate.setDeleted(false);
		toCreate.setCreationDate(new Date());
		
		EventTypeEntity eventType = new EventTypeEntity();
		eventType.setId(dto.getEventTypeId());
		eventType.setName(dto.getEventTypeName());
		eventType.setCreationDate(new Date());
		eventType.setLastModifiedDate(new Date());
		eventType.setDeleted(false);
		eventType.setLastModifiedUser(Util.getCurrentUser().getId());
		toCreate.setEventType(eventType);
		
		entityManager.persist(toCreate);
		return new CertificationEventDTO(toCreate);
	}

	@Override
	public List<CertificationEventDTO> findAll() {
		List<CertificationEventEntity> result = this.findAllEntities();
		List<CertificationEventDTO> dtos = new ArrayList<CertificationEventDTO>(result.size());
		for(CertificationEventEntity entity : result) {
			dtos.add(new CertificationEventDTO(entity));
		}
		return dtos;
	}
	
	@Override
	public CertificationEventDTO findById(Long eventId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificationEventDTO findByEventTypeName(String eventTypeName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private List<CertificationEventEntity> findAllEntities() {
		Query query = entityManager.createQuery("SELECT ce from CertificationEventEntity ce where (NOT ce.deleted = true)");
		return query.getResultList();
	}
	
	public CertificationEventEntity getEntityById(Long id) throws EntityRetrievalException {
		CertificationEventEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationEventEntity ce where (NOT deleted = true) AND (certification_event_id = :entityid) ", CertificationEventEntity.class );
		query.setParameter("entityid", id);
		List<CertificationEventEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
	
	public CertificationEventEntity getEntityByEventTypeName(String eventTypeName) throws EntityRetrievalException {
		CertificationEventEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationEventEntity ce FETCH JOIN EventTypeEntity e "
				+ "where (NOT ce.deleted = true) AND (e.name = :eventTypeName) ", CertificationEventEntity.class );
		query.setParameter("eventTypeName", eventTypeName);
		List<CertificationEventEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certification event id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		
		return entity;
	}
}
