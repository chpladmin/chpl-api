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
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;


@Repository(value="certificationBodyDAO")
public class CertificationBodyDAOImpl extends BaseDAOImpl implements CertificationBodyDAO {
	
	private static final Logger logger = LogManager.getLogger(CertificationBodyDAOImpl.class);
	@Autowired AddressDAO addressDao;
	
	@Transactional
	public CertificationBodyDTO create(CertificationBodyDTO dto) throws EntityRetrievalException, EntityCreationException {
		CertificationBodyEntity entity = null;
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
			entity = new CertificationBodyEntity();

			if(dto.getAddress() != null)
			{
				entity.setAddress(addressDao.mergeAddress(dto.getAddress()));
			}
			
			entity.setName(dto.getName());
			entity.setWebsite(dto.getWebsite());
			entity.setAcbCode(dto.getAcbCode());
			
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
			return new CertificationBodyDTO(entity);
		}	
	}

	@Transactional
	public CertificationBodyDTO update(CertificationBodyDTO dto) throws EntityRetrievalException{
		
		CertificationBodyEntity entity = getEntityById(dto.getId(), true);	
		if(entity == null) {
			throw new EntityRetrievalException("Cannot update entity with id " + dto.getId() + ". Entity does not exist.");
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
		if(dto.getName() != null) {
			entity.setName(dto.getName());
		}
		
		if(dto.getAcbCode() != null) {
			entity.setAcbCode(dto.getAcbCode());
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
		return new CertificationBodyDTO(entity);
	}
	
	@Transactional
	public void delete(Long acbId){
		
		// TODO: How to delete this without leaving orphans
		
		Query query = entityManager.createQuery("UPDATE CertificationBodyEntity SET deleted = true WHERE certification_body_id = :acbid");
		query.setParameter("acbid", acbId);
		query.executeUpdate();
		
		Query query2 = entityManager.createQuery("UPDATE ActivityEntity SET deleted = true WHERE activity_object_id = :acbid AND description NOT LIKE '%Deleted acb%' AND description NOT LIKE '%no longer marked as deleted%'");
		query2.setParameter("acbid", acbId);
		query2.executeUpdate();
		
	}
	
	public List<CertificationBodyDTO> findAll(boolean showDeleted){
		
		List<CertificationBodyEntity> entities = getAllEntities(showDeleted);
		List<CertificationBodyDTO> acbs = new ArrayList<>();
		
		for (CertificationBodyEntity entity : entities) {
			CertificationBodyDTO acb = new CertificationBodyDTO(entity);
			acbs.add(acb);
		}
		return acbs;
		
	}
	
	public CertificationBodyDTO getById(Long acbId) throws EntityRetrievalException{
		CertificationBodyEntity entity = getEntityById(acbId, false);
		
		CertificationBodyDTO dto = null;
		if(entity != null) {
			dto = new CertificationBodyDTO(entity);
		}
		return dto;
		
	}
	
	public CertificationBodyDTO getById(Long acbId, boolean includeDeleted) throws EntityRetrievalException{
		CertificationBodyEntity entity = getEntityById(acbId, includeDeleted);
		
		CertificationBodyDTO dto = null;
		if(entity != null) {
			dto = new CertificationBodyDTO(entity);
		}
		return dto;
		
	}
	
	public CertificationBodyDTO getByName(String name) {
		CertificationBodyEntity entity = getEntityByName(name);
		
		CertificationBodyDTO dto = null;
		if(entity != null) {
			dto = new CertificationBodyDTO(entity);
		}
		return dto;
	}
	
	public String getMaxCode() {
		String maxCode = null;
		Query query = entityManager.createQuery( "SELECT acb.acbCode "
				+ "from CertificationBodyEntity acb "
				+ "ORDER BY acb.acbCode DESC", String.class );
		List<String> result = query.getResultList();
		
		if(result != null && result.size() > 0) {
			maxCode = result.get(0);
		}
		return maxCode;
	}
	
	private void create(CertificationBodyEntity acb) {
		
		entityManager.persist(acb);
		entityManager.flush();
	}
	
	private void update(CertificationBodyEntity acb) {
		
		entityManager.merge(acb);
		entityManager.flush();
	
	}
	
	private List<CertificationBodyEntity> getAllEntities(boolean showDeleted) {
		
		List<CertificationBodyEntity> result;
		
		if(showDeleted){
			result = entityManager.createQuery( "SELECT acb from CertificationBodyEntity acb LEFT OUTER JOIN FETCH acb.address", CertificationBodyEntity.class).getResultList();
		}else{
			result = entityManager.createQuery( "SELECT acb from CertificationBodyEntity acb LEFT OUTER JOIN FETCH acb.address where (NOT acb.deleted = true)", CertificationBodyEntity.class).getResultList();
		}
		return result;
	}
	
	private CertificationBodyEntity getEntityById(Long entityId, boolean includeDeleted) throws EntityRetrievalException {
		
		CertificationBodyEntity entity = null;
		
		String queryStr = "SELECT acb from CertificationBodyEntity acb "
				+ "LEFT OUTER JOIN FETCH acb.address where "
				+ "(certification_body_id = :entityid)";
		if(!includeDeleted) {
			queryStr += " AND (NOT acb.deleted = true)";
		}
		Query query = entityManager.createQuery(queryStr, CertificationBodyEntity.class );
		query.setParameter("entityid", entityId);
		List<CertificationBodyEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certificaiton body id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}

		return entity;
	}
	
	private CertificationBodyEntity getEntityByName(String name) {
		
		CertificationBodyEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT acb from CertificationBodyEntity acb LEFT OUTER JOIN FETCH acb.address where (NOT acb.deleted = true) AND (name = :name) ", CertificationBodyEntity.class );
		query.setParameter("name", name);
		List<CertificationBodyEntity> result = query.getResultList();
		
		if(result != null && result.size() > 0) {
			entity = result.get(0);
		}
		return entity;
	}
	
}
