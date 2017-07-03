package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestTaskDAO;
import gov.healthit.chpl.dto.TestTaskDTO;
import gov.healthit.chpl.entity.TestTaskEntity;

@Repository("testTaskDao")
public class TestTaskDAOImpl extends BaseDAOImpl implements TestTaskDAO {
	
	@Override
	public TestTaskDTO create(TestTaskDTO dto) throws EntityCreationException {
		
		TestTaskEntity entity = null;
		if (dto.getId() != null){
			entity = this.getEntityById(dto.getId());
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			entity = new TestTaskEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setDescription(dto.getDescription());
			entity.setTaskErrors(dto.getTaskErrors());
			entity.setTaskErrorsStddev(dto.getTaskErrorsStddev());
			entity.setTaskPathDeviationObserved(dto.getTaskPathDeviationObserved());
			entity.setTaskPathDeviationOptimal(dto.getTaskPathDeviationOptimal());
			entity.setTaskRating(dto.getTaskRating());
			entity.setTaskRatingScale(dto.getTaskRatingScale());
			entity.setTaskRatingStddev(dto.getTaskRatingStddev());
			entity.setTaskSuccessAverage(dto.getTaskSuccessAverage());
			entity.setTaskSuccessStddev(dto.getTaskSuccessStddev());
			entity.setTaskTimeAvg(dto.getTaskTimeAvg());
			entity.setTaskTimeDeviationObservedAvg(dto.getTaskTimeDeviationObservedAvg());
			entity.setTaskTimeDeviationOptimalAvg(dto.getTaskTimeDeviationOptimalAvg());
			entity.setTaskTimeStddev(dto.getTaskTimeStddev());
			
			create(entity);
			return new TestTaskDTO(entity);
		}		
	}

	@Override
	public TestTaskDTO update(TestTaskDTO dto)
			throws EntityRetrievalException {
		TestTaskEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setDescription(dto.getDescription());
		entity.setTaskErrors(dto.getTaskErrors());
		entity.setTaskErrorsStddev(dto.getTaskErrorsStddev());
		entity.setTaskPathDeviationObserved(dto.getTaskPathDeviationObserved());
		entity.setTaskPathDeviationOptimal(dto.getTaskPathDeviationOptimal());
		entity.setTaskRating(dto.getTaskRating());
		entity.setTaskRatingScale(dto.getTaskRatingScale());
		entity.setTaskRatingStddev(dto.getTaskRatingStddev());
		entity.setTaskSuccessAverage(dto.getTaskSuccessAverage());
		entity.setTaskSuccessStddev(dto.getTaskSuccessStddev());
		entity.setTaskTimeAvg(dto.getTaskTimeAvg());
		entity.setTaskTimeDeviationObservedAvg(dto.getTaskTimeDeviationObservedAvg());
		entity.setTaskTimeDeviationOptimalAvg(dto.getTaskTimeDeviationOptimalAvg());
		entity.setTaskTimeStddev(dto.getTaskTimeStddev());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
		update(entity);
		return new TestTaskDTO(entity);
	}

	@Override
	public void delete(Long id) {
		
		TestTaskEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public TestTaskDTO getById(Long id) {
		
		TestTaskDTO dto = null;
		TestTaskEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new TestTaskDTO(entity);
		}
		return dto;
	}
	
	@Override
	public List<TestTaskDTO> findAll() {
		
		List<TestTaskEntity> entities = getAllEntities();
		List<TestTaskDTO> dtos = new ArrayList<TestTaskDTO>();
		
		for (TestTaskEntity entity : entities) {
			TestTaskDTO dto = new TestTaskDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(TestTaskEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(TestTaskEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TestTaskEntity> getAllEntities() {
		return entityManager.createQuery( "from TestTaskEntity where (NOT deleted = true) ", TestTaskEntity.class).getResultList();
	}
	
	private TestTaskEntity getEntityById(Long id) {
		
		TestTaskEntity entity = null;
			
		Query query = entityManager.createQuery( "from TestTaskEntity where (NOT deleted = true) AND (id = :entityid) ", TestTaskEntity.class );
		query.setParameter("entityid", id);
		List<TestTaskEntity> result = query.getResultList();

		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}