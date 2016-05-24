package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.entity.CertifiedProductQmsStandardEntity;

@Repository(value="certifiedProductQmsStandardDao")
public class CertifiedProductQmsStandardDAOImpl extends BaseDAOImpl implements CertifiedProductQmsStandardDAO {

	@Override
	public CertifiedProductQmsStandardDTO createCertifiedProductQms(CertifiedProductQmsStandardDTO toCreate) throws EntityCreationException {
		
		CertifiedProductQmsStandardEntity toCreateEntity = new CertifiedProductQmsStandardEntity();
		toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
		toCreateEntity.setQmsStandardId(toCreate.getQmsStandardId());
		toCreateEntity.setApplicableCriteria(toCreate.getApplicableCriteria());
		toCreateEntity.setModification(toCreate.getQmsModification());
		toCreateEntity.setLastModifiedDate(new Date());
		toCreateEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		toCreateEntity.setCreationDate(new Date());
		toCreateEntity.setDeleted(false);
		entityManager.persist(toCreateEntity);
		entityManager.flush();

		return new CertifiedProductQmsStandardDTO(toCreateEntity);
	}
	
	@Override
	public CertifiedProductQmsStandardDTO updateCertifiedProductQms(CertifiedProductQmsStandardDTO toUpdate) throws EntityRetrievalException {
		
		CertifiedProductQmsStandardEntity curr = getEntityById(toUpdate.getId());
		if(curr == null) {
			throw new EntityRetrievalException("Could not find mapping with id " + toUpdate.getId());
		}
		curr.setApplicableCriteria(toUpdate.getApplicableCriteria());
		curr.setModification(toUpdate.getQmsModification());
		curr.setLastModifiedDate(new Date());
		curr.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.merge(curr);
		entityManager.flush();

		return new CertifiedProductQmsStandardDTO(curr);
	}
	
	@Override
	public CertifiedProductQmsStandardDTO deleteCertifiedProductQms(Long id) throws EntityRetrievalException {
		
		CertifiedProductQmsStandardEntity curr = getEntityById(id);
		if(curr == null) {
			throw new EntityRetrievalException("Could not find mapping with id " + id);
		}
		curr.setDeleted(true);
		curr.setLastModifiedDate(new Date());
		curr.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(curr);
		entityManager.flush();

		return new CertifiedProductQmsStandardDTO(curr);
	}
	
	@Override
	public List<CertifiedProductQmsStandardDTO> getQmsStandardsByCertifiedProductId(Long certifiedProductId)
			throws EntityRetrievalException {
		List<CertifiedProductQmsStandardEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CertifiedProductQmsStandardDTO> dtos = new ArrayList<CertifiedProductQmsStandardDTO>();
		
		for (CertifiedProductQmsStandardEntity entity : entities){
			dtos.add(new CertifiedProductQmsStandardDTO(entity));
		}
		return dtos;
	}
	
	private CertifiedProductQmsStandardEntity getEntityById(Long id) throws EntityRetrievalException {
		CertifiedProductQmsStandardEntity entity = null;
		Query query = entityManager.createQuery( "SELECT qms from CertifiedProductQmsStandardEntity qms "
				+ "LEFT OUTER JOIN FETCH qms.qmsStandard "
				+ "where (NOT qms.deleted = true) AND (certified_product_qms_standard_id = :entityid) ", 
				CertifiedProductQmsStandardEntity.class );

		query.setParameter("entityid", id);
		List<CertifiedProductQmsStandardEntity> result = query.getResultList();
		if(result.size() >= 1) {
			entity = result.get(0);
		} 
		return entity;
	}
	
	private List<CertifiedProductQmsStandardEntity> getEntitiesByCertifiedProductId(Long productId) throws EntityRetrievalException {
		Query query = entityManager.createQuery( "SELECT qms from CertifiedProductQmsStandardEntity qms "
				+ "LEFT OUTER JOIN FETCH qms.qmsStandard "
				+ "where (NOT qms.deleted = true) AND (certified_product_id = :entityid) ", 
				CertifiedProductQmsStandardEntity.class );

		query.setParameter("entityid", productId);
		List<CertifiedProductQmsStandardEntity> result = query.getResultList();
		
		return result;
	}
	
}
