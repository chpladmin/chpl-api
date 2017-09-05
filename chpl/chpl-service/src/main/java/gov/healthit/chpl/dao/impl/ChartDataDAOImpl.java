package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.ChartDataDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.ChartDataEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository(value="chartDataDAO")
public class ChartDataDAOImpl extends BaseDAOImpl implements ChartDataDAO{

	private static final Logger logger = LogManager.getLogger(ChartDataDAOImpl.class);

	@Transactional
	public ChartDataDTO create(ChartDataDTO dto) throws EntityRetrievalException, EntityCreationException {
		ChartDataEntity entity = null;
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
			entity = new ChartDataEntity();
			
			entity.setDataDate(dto.getDataDate());
			entity.setJsonDataObject(dto.getJsonDataObject());
			entity.setTypeOfStatId(dto.getTypeOfStatId());
			
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
			
			create(entity);
			return new ChartDataDTO(entity);
		}
	}
	
	@Transactional
	public ChartDataDTO update(ChartDataDTO dto) throws EntityRetrievalException{
		
		ChartDataEntity entity = getEntityById(dto.getId());	
		if(entity == null) {
			throw new EntityRetrievalException("Cannot update entity with id " + dto.getId() + ". Entity does not exist.");
		}
		
		entity.setDataDate(new Date());
		if(dto.getJsonDataObject() != null) {
			entity.setJsonDataObject(dto.getJsonDataObject());
		}
		
		if(dto.getTypeOfStatId() != null) {
			entity.setTypeOfStatId(dto.getTypeOfStatId());
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
		return new ChartDataDTO(entity);
	}
	
	public ChartDataDTO getById(Long acbId) throws EntityRetrievalException{
		ChartDataEntity entity = this.getEntityById(acbId);
		
		ChartDataDTO dto = null;
		if(entity != null) {
			dto = new ChartDataDTO(entity);
		}
		return dto;
		
	}

	public List<ChartDataDTO> findAll(){

		List<ChartDataEntity> entities = getAllEntities();
		List<ChartDataDTO> acbs = new ArrayList<>();

		for (ChartDataEntity entity : entities) {
			ChartDataDTO acb = new ChartDataDTO(entity);
			acbs.add(acb);
		}
		return acbs;

	}
	
	public List<ChartDataEntity> getAllEntities() {
		
		List<ChartDataEntity> result;
		
		result = entityManager.createQuery( "from ChartDataEntity", ChartDataEntity.class).getResultList();

		return result;
	}

	public ChartDataEntity getEntityById(Long entityId) throws EntityRetrievalException {

		ChartDataEntity entity = null;

		String queryStr = "from ChartDataEntity "
				+ "where (chart_data_id = :entityid)";
		Query query = entityManager.createQuery(queryStr, ChartDataEntity.class );
		query.setParameter("entityid", entityId);
		List<ChartDataEntity> result = query.getResultList();

		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate certificaiton body id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}

		return entity;
	}

	private void create(ChartDataEntity cd) {
		entityManager.persist(cd);
		entityManager.flush();
	}

	private void update(ChartDataEntity cd) {
		entityManager.merge(cd);
		entityManager.flush();
	}

}
