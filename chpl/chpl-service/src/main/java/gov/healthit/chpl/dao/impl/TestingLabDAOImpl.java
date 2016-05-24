package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.TestingLabEntity;

@Repository("testingLabDAO")
public class TestingLabDAOImpl extends BaseDAOImpl implements TestingLabDAO {

	private static final Logger logger = LogManager.getLogger(TestingLabDAOImpl.class);
	@Autowired AddressDAO addressDao;
	
	@Override
	@Transactional
	public TestingLabDTO create(TestingLabDTO dto) throws EntityCreationException, EntityRetrievalException {
		
		TestingLabEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId(), false);
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {			
			entity = new TestingLabEntity();

			if(dto.getAddress() != null)
			{
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
			}
			
			entity.setName(dto.getName());
			entity.setWebsite(dto.getWebsite());
			entity.setAccredidationNumber(dto.getAccredidationNumber());
			entity.setTestingLabCode(dto.getTestingLabCode());
			
			if(dto.getDeleted() != null) {
				entity.setDeleted(dto.getDeleted());
			} else {
				entity.setDeleted(false);
			}
			
			if(dto.getLastModifiedUser() != null) {
				entity.setLastModifiedUser(dto.getLastModifiedUser());
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}		
			
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			
			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}
			
			create(entity);
			return new TestingLabDTO(entity);
		}	
	}
	
	@Override
	@Transactional
	public TestingLabDTO update(TestingLabDTO dto) throws EntityRetrievalException {
		TestingLabEntity entity = this.getEntityById(dto.getId(), true);
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		if(dto.getAddress() != null)
		{
			try {
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
			} catch(EntityCreationException ex) {
				logger.error("Could not create new address in the database.", ex);
				entity.setAddress(null);
			}
		} else {
			entity.setAddress(null);
		}
		
		entity.setWebsite(dto.getWebsite());
		entity.setAccredidationNumber(dto.getAccredidationNumber());

		if(dto.getName() != null) {
			entity.setName(dto.getName());
		}
		
		if(dto.getTestingLabCode() != null) {
			entity.setTestingLabCode(dto.getTestingLabCode());
		}
		
		if(dto.getDeleted() != null) {
			if(!dto.getDeleted()){
				Query query2 = entityManager.createQuery("UPDATE ActivityEntity SET deleted = false WHERE activity_object_id = :acbid");
				query2.setParameter("acbid", dto.getId());
				query2.executeUpdate();
			}
			entity.setDeleted(dto.getDeleted());
		}
		
		if(dto.getLastModifiedUser() != null) {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		}		
		
		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}
			
		update(entity);
		return new TestingLabDTO(entity);
	}
	
	@Override
	@Transactional
	public void delete(Long id) throws EntityRetrievalException {
		TestingLabEntity toDelete = getEntityById(id, false);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
			
			Query query = entityManager.createQuery("UPDATE ActivityEntity SET deleted = true WHERE activity_object_id = :atlid AND description NOT LIKE '%Deleted testing lab%' AND description NOT LIKE '%no longer marked as deleted%'");
			query.setParameter("atlid", toDelete.getId());
			query.executeUpdate();
		}
	}
	
	@Override
	public List<TestingLabDTO> findAll(boolean showDeleted) {
		
		List<TestingLabEntity> entities = getAllEntities(showDeleted);
		List<TestingLabDTO> dtos = new ArrayList<>();
		
		for (TestingLabEntity entity : entities) {
			TestingLabDTO dto = new TestingLabDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public TestingLabDTO getById(Long id) throws EntityRetrievalException {
		
		TestingLabEntity entity = getEntityById(id, false);
		TestingLabDTO dto = null;
		if(entity != null) {
			dto = new TestingLabDTO(entity);
		}
		return dto;
	}
	
	@Override
	public TestingLabDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		
		TestingLabEntity entity = getEntityById(id, includeDeleted);
		TestingLabDTO dto = null;
		if(entity != null) {
			dto = new TestingLabDTO(entity);
		}
		return dto;
	}
	
	@Override
	public TestingLabDTO getByName(String name) {
		TestingLabEntity entity = getEntityByName(name);
		TestingLabDTO dto = null;
		if(entity != null) {
			dto = new TestingLabDTO(entity);
		}
		return dto;
	}
	
	public String getMaxCode() {
		String maxCode = null;
		Query query = entityManager.createQuery( "SELECT atl.testingLabCode "
				+ "from TestingLabEntity atl "
				+ "ORDER BY atl.testingLabCode DESC", String.class );
		List<String> result = query.getResultList();
		
		if(result != null && result.size() > 0) {
			maxCode = result.get(0);
		}
		return maxCode;
	}
	
	private void create(TestingLabEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(TestingLabEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TestingLabEntity> getAllEntities(boolean showDeleted) {
		List<TestingLabEntity> result = null;
		if(showDeleted){
			result = entityManager.createQuery( "SELECT atl from TestingLabEntity atl "
					+ "LEFT OUTER JOIN FETCH atl.address ", TestingLabEntity.class).getResultList();
		}else{
			result = entityManager.createQuery( "SELECT atl from TestingLabEntity atl "
					+ "LEFT OUTER JOIN FETCH atl.address "
					+ "where (NOT atl.deleted = true)", TestingLabEntity.class).getResultList();
		}
		return result;
	}

	private TestingLabEntity getEntityById(Long id, boolean includeDeleted) throws EntityRetrievalException {
		
		TestingLabEntity entity = null;
			
		String queryStr = "SELECT atl from TestingLabEntity atl "
				+ "LEFT OUTER JOIN FETCH atl.address "
				+ "where (testing_lab_id = :entityid) ";
		if(!includeDeleted) {
			queryStr += " AND (NOT atl.deleted = true)";
		}
		Query query = entityManager.createQuery(queryStr, TestingLabEntity.class );
		query.setParameter("entityid", id);
		List<TestingLabEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate testing lab id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		return entity;
	}
	
	private TestingLabEntity getEntityByName(String name) {
		
		TestingLabEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT atl from TestingLabEntity atl "
				+ "LEFT OUTER JOIN FETCH atl.address "
				+ "where (NOT atl.deleted = true) "
				+ "AND (atl.name = :name) ", TestingLabEntity.class );
		query.setParameter("name", name);
		List<TestingLabEntity> result = query.getResultList();
		
		if(result.size() > 0) {
			entity = result.get(0);
		}

		return entity;
	}
}
