package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDao;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.entity.VendorEntity;

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

	public List<PendingCertifiedProductDTO> findAll() {
		List<PendingCertifiedProductEntity> entities = getAllEntities();
		List<PendingCertifiedProductDTO> dtos = new ArrayList<>();
		
		for (PendingCertifiedProductEntity entity : entities) {
			PendingCertifiedProductDTO dto = new PendingCertifiedProductDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public PendingCertifiedProductDTO findById(Long pcpId) throws EntityRetrievalException {
		PendingCertifiedProductEntity entity = getEntityById(pcpId);
		return new PendingCertifiedProductDTO(entity);
	}
	
	private void update(PendingCertifiedProductEntity product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<PendingCertifiedProductEntity> getAllEntities() {
		
		List<PendingCertifiedProductEntity> result = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " LEFT OUTER JOIN FETCH pcp.certificationCriterion"
				+ " LEFT OUTER JOIN FETCH pcp.cqmCriterion", PendingCertifiedProductEntity.class).getResultList();
		return result;
		
	}
	
	private PendingCertifiedProductEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		PendingCertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "SELECT pcp from PendingCertifiedProductEntity pcp "
				+ " LEFT OUTER JOIN FETCH pcp.certificationCriterion"
				+ " LEFT OUTER JOIN FETCH pcp.cqmCriterion"
				+ " where (pending_certified_product_id = :entityid) ", PendingCertifiedProductEntity.class );
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
