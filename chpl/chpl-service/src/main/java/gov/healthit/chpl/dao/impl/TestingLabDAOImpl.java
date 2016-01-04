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
				entity = this.getEntityById(dto.getId());
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
		TestingLabEntity entity = this.getEntityById(dto.getId());
		
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
		TestingLabEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}
	
	@Override
	public List<TestingLabDTO> findAll() {
		
		List<TestingLabEntity> entities = getAllEntities();
		List<TestingLabDTO> dtos = new ArrayList<>();
		
		for (TestingLabEntity entity : entities) {
			TestingLabDTO dto = new TestingLabDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public TestingLabDTO getById(Long id) throws EntityRetrievalException {
		
		TestingLabEntity entity = getEntityById(id);
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
	
	private void create(TestingLabEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(TestingLabEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TestingLabEntity> getAllEntities() {
		List<TestingLabEntity> result = entityManager.createQuery( "SELECT atl from TestingLabEntity atl "
				+ "LEFT OUTER JOIN FETCH atl.address "
				+ "where (NOT atl.deleted = true)", TestingLabEntity.class).getResultList();
		return result;
		
	}

	private TestingLabEntity getEntityById(Long id) throws EntityRetrievalException {
		
		TestingLabEntity entity = null;
			
		Query query = entityManager.createQuery( "SELECT atl from TestingLabEntity atl "
				+ "LEFT OUTER JOIN FETCH atl.address "
				+ "where (NOT atl.deleted = true) "
				+ "AND (testing_lab_id = :entityid) ", TestingLabEntity.class );
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
