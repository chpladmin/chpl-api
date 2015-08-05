package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.CqmResultEntity;

public class CQMResultDAOImpl extends BaseDAOImpl implements CQMResultDAO {

	@Override
	public void create(CQMResultDTO cqmResult) throws EntityCreationException {
		
		CqmResultEntity entity = null;
		try {
			if (cqmResult.getId() != null){
				entity = this.getEntityById(cqmResult.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("A product with this ID already exists.");
		} else {
			
			entity = new CqmResultEntity();
			//entity.setCertifiedProductCqmEditionMap();
			entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
			entity.setCqmVersionId(cqmResult.getCqmVersionId());
			entity.setCreationDate(cqmResult.getCreationDate());
			entity.setDeleted(cqmResult.getDeleted());
			entity.setId(cqmResult.getId());
			entity.setLastModifiedDate(cqmResult.getLastModifiedDate());
			entity.setLastModifiedUser(cqmResult.getLastModifiedUser());
			entity.setSuccess(cqmResult.getSuccess());
			
			create(entity);	
		}
		
	}
	
	@Override
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException {
		
		CqmResultEntity entity = this.getEntityById(cqmResult.getId());
		//entity.setCertifiedProductCqmEditionMap();
		entity.setCqmCriterionId(cqmResult.getCqmCriterionId());
		entity.setCqmVersionId(cqmResult.getCqmVersionId());
		entity.setCreationDate(cqmResult.getCreationDate());
		entity.setDeleted(cqmResult.getDeleted());
		entity.setId(cqmResult.getId());
		entity.setLastModifiedDate(cqmResult.getLastModifiedDate());
		entity.setLastModifiedUser(cqmResult.getLastModifiedUser());
		entity.setSuccess(cqmResult.getSuccess());
		
		create(entity);	
		
	}

	private CqmResultEntity getEntityById(Long id) throws EntityRetrievalException {
			
		CqmResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertifiedProduct where (NOT deleted = true) AND (cqm_result_id = :entityid) ", CqmResultEntity.class );
		query.setParameter("entityid", id);
		List<CqmResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
			
		return entity;
	}

	@Override
	public void delete(Long cqmResultId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<CQMResultDTO> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CQMResultDTO getById(Long cqmResultId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void create(CqmResultEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(CqmResultEntity entity) {
		
		entityManager.merge(entity);	
	
	}

}
