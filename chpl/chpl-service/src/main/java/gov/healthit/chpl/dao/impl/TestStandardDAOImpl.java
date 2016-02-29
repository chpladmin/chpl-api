package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.entity.TestStandardEntity;

@Repository("testStandardDAO")
public class TestStandardDAOImpl extends BaseDAOImpl implements TestStandardDAO {
	
	@Override
	public TestStandardDTO create(TestStandardDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		TestStandardEntity entity = null;
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
			entity = new TestStandardEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			entity.setNumber(dto.getNumber());
			create(entity);
			return new TestStandardDTO(entity);
		}		
	}

	@Override
	public TestStandardDTO update(TestStandardDTO dto)
			throws EntityRetrievalException {
		TestStandardEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setName(dto.getName());
		entity.setNumber(dto.getNumber());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
		update(entity);
		return new TestStandardDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		TestStandardEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public TestStandardDTO getById(Long id)
			throws EntityRetrievalException {
		
		TestStandardDTO dto = null;
		TestStandardEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new TestStandardDTO(entity);
		}
		return dto;
	}
	
	@Override
	public TestStandardDTO getByNumber(String name) {
		
		TestStandardDTO dto = null;
		List<TestStandardEntity> entities = getEntitiesByNumber(name);
		
		if (entities != null && entities.size() > 0){
			dto = new TestStandardDTO(entities.get(0));
		}
		return dto;
	}
	
	@Override
	public List<TestStandardDTO> findAll() {
		
		List<TestStandardEntity> entities = getAllEntities();
		List<TestStandardDTO> dtos = new ArrayList<TestStandardDTO>();
		
		for (TestStandardEntity entity : entities) {
			TestStandardDTO dto = new TestStandardDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(TestStandardEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(TestStandardEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TestStandardEntity> getAllEntities() {
		return entityManager.createQuery( "from TestStandardEntity where (NOT deleted = true) ", TestStandardEntity.class).getResultList();
	}
	
	private TestStandardEntity getEntityById(Long id) throws EntityRetrievalException {
		
		TestStandardEntity entity = null;
			
		Query query = entityManager.createQuery( "from TestStandardEntity where (NOT deleted = true) AND (test_standard_id = :entityid) ", TestStandardEntity.class );
		query.setParameter("entityid", id);
		List<TestStandardEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate qms standard id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<TestStandardEntity> getEntitiesByNumber(String number) {
		
		Query query = entityManager.createQuery( "from TestStandardEntity where (NOT deleted = true) AND (number = :number) ", TestStandardEntity.class );
		query.setParameter("number", number);
		List<TestStandardEntity> result = query.getResultList();
		
		return result;
	}
	
	
}