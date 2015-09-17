package gov.healthit.chpl.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDao;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

@Repository(value="pendingCertifiedProductDAO")
public class PendingCertifiedProductDaoImpl extends BaseDAOImpl implements PendingCertifiedProductDao {
	
	@Override
	@Transactional
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity toCreate) {		
		entityManager.persist(toCreate);
		
		for(PendingCertificationCriterionEntity criterion : toCreate.getCertificationCriterion()) {
			criterion.setPendingCertifiedProductId(toCreate.getId());
			entityManager.persist(criterion);
		}
		
		for(PendingCqmCriterionEntity cqm : toCreate.getCqmCriterion()) {
			cqm.setPendingCertifiedProductId(toCreate.getId());
			entityManager.persist(cqm);
		}
		
		return new PendingCertifiedProductDTO(toCreate);
	}

	
	private void update(PendingCertifiedProductEntity product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<PendingCertifiedProductEntity> getAllEntities() {
		
		List<PendingCertifiedProductEntity> result = entityManager.createQuery( "from PendingCertifiedProductEntity where (NOT deleted = true) ", PendingCertifiedProductEntity.class).getResultList();
		return result;
		
	}
	
	private PendingCertifiedProductEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		PendingCertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "from PendingCertifiedProductEntity where (pending_certified_product_id = :entityid) ", PendingCertifiedProductEntity.class );
		query.setParameter("entityid", entityId);
		List<PendingCertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
}
