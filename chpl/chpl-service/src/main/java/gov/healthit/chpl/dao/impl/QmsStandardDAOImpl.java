package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.entity.QmsStandardEntity;

@Repository("qmsStandardDao")
public class QmsStandardDAOImpl extends BaseDAOImpl implements QmsStandardDAO {
	
	@Override
	public QmsStandardDTO create(QmsStandardDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		QmsStandardEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			entity = new QmsStandardEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			create(entity);
			return new QmsStandardDTO(entity);
		}		
	}

	@Override
	public QmsStandardDTO update(QmsStandardDTO dto)
			throws EntityRetrievalException {
		QmsStandardEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setName(dto.getName());
		
		update(entity);
		return new QmsStandardDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		QmsStandardEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public QmsStandardDTO getById(Long id)
			throws EntityRetrievalException {
		
		QmsStandardDTO dto = null;
		QmsStandardEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new QmsStandardDTO(entity);
		}
		return dto;
	}
	
	@Override
	public QmsStandardDTO getByName(String name) {
		
		QmsStandardDTO dto = null;
		List<QmsStandardEntity> entities = getEntitiesByName(name);
		
		if (entities != null && entities.size() > 0){
			dto = new QmsStandardDTO(entities.get(0));
		}
		return dto;
	}
	
	@Override
	public List<QmsStandardDTO> findAll() {
		
		List<QmsStandardEntity> entities = getAllEntities();
		List<QmsStandardDTO> dtos = new ArrayList<QmsStandardDTO>();
		
		for (QmsStandardEntity entity : entities) {
			QmsStandardDTO dto = new QmsStandardDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(QmsStandardEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(QmsStandardEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<QmsStandardEntity> getAllEntities() {
		return entityManager.createQuery( "from QmsStandardEntity where (NOT deleted = true) ", QmsStandardEntity.class).getResultList();
	}
	
	private QmsStandardEntity getEntityById(Long id) throws EntityRetrievalException {
		
		QmsStandardEntity entity = null;
			
		Query query = entityManager.createQuery( "from QmsStandardEntity where (NOT deleted = true) AND (qms_standard_id = :entityid) ", QmsStandardEntity.class );
		query.setParameter("entityid", id);
		List<QmsStandardEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate qms standard id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<QmsStandardEntity> getEntitiesByName(String name) {
		
		Query query = entityManager.createQuery( "from QmsStandardEntity where (NOT deleted = true) AND (name = :name) ", QmsStandardEntity.class );
		query.setParameter("name", name);
		List<QmsStandardEntity> result = query.getResultList();
		
		return result;
	}
	
	
}