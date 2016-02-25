package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.entity.TestFunctionalityEntity;

@Repository("testFunctionalityDAO")
public class TestFunctionalityDAOImpl extends BaseDAOImpl implements TestFunctionalityDAO {
	
	@Override
	public TestFunctionalityDTO create(TestFunctionalityDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		TestFunctionalityEntity entity = null;
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
			entity = new TestFunctionalityEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			entity.setNumber(dto.getNumber());
			
			create(entity);
			return new TestFunctionalityDTO(entity);
		}		
	}

	@Override
	public TestFunctionalityDTO update(TestFunctionalityDTO dto)
			throws EntityRetrievalException {
		TestFunctionalityEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setName(dto.getName());
		entity.setNumber(dto.getNumber());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
		update(entity);
		return new TestFunctionalityDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		TestFunctionalityEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public TestFunctionalityDTO getById(Long id)
			throws EntityRetrievalException {
		
		TestFunctionalityDTO dto = null;
		TestFunctionalityEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new TestFunctionalityDTO(entity);
		}
		return dto;
	}
	
	@Override
	public TestFunctionalityDTO getByNumber(String number) {
		
		TestFunctionalityDTO dto = null;
		List<TestFunctionalityEntity> entities = getEntitiesByNumber(number);
		
		if (entities != null && entities.size() > 0){
			dto = new TestFunctionalityDTO(entities.get(0));
		}
		return dto;
	}
	
	@Override
	public List<TestFunctionalityDTO> findAll() {
		
		List<TestFunctionalityEntity> entities = getAllEntities();
		List<TestFunctionalityDTO> dtos = new ArrayList<TestFunctionalityDTO>();
		
		for (TestFunctionalityEntity entity : entities) {
			TestFunctionalityDTO dto = new TestFunctionalityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(TestFunctionalityEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(TestFunctionalityEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TestFunctionalityEntity> getAllEntities() {
		return entityManager.createQuery( "from TestFunctionalityEntity where (NOT deleted = true) ", TestFunctionalityEntity.class).getResultList();
	}
	
	private TestFunctionalityEntity getEntityById(Long id) throws EntityRetrievalException {
		
		TestFunctionalityEntity entity = null;
			
		Query query = entityManager.createQuery( "from TestFunctionalityEntity where (NOT deleted = true) AND (id = :entityid) ", TestFunctionalityEntity.class );
		query.setParameter("entityid", id);
		List<TestFunctionalityEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate test functionality id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private List<TestFunctionalityEntity> getEntitiesByNumber(String number) {
		
		TestFunctionalityEntity entity = null;
			
		Query query = entityManager.createQuery( "from TestFunctionalityEntity where (NOT deleted = true) AND (number = :number) ", TestFunctionalityEntity.class );
		query.setParameter("number", number);
		return query.getResultList();
	}
}