package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.entity.CertifiedProductTargetedUserEntity;

@Repository(value="certifiedProductTargetedUserDao")
public class CertifiedProductTargetedUserDAOImpl extends BaseDAOImpl 
	implements CertifiedProductTargetedUserDAO {

	@Override
	public CertifiedProductTargetedUserDTO createCertifiedProductTargetedUser(CertifiedProductTargetedUserDTO toCreate) throws EntityCreationException {
		
		CertifiedProductTargetedUserEntity toCreateEntity = new CertifiedProductTargetedUserEntity();
		toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
		toCreateEntity.setTargetedUserId(toCreate.getTargetedUserId());
		toCreateEntity.setLastModifiedDate(new Date());
		toCreateEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		toCreateEntity.setCreationDate(new Date());
		toCreateEntity.setDeleted(false);
		entityManager.persist(toCreateEntity);
		entityManager.flush();

		return new CertifiedProductTargetedUserDTO(toCreateEntity);
	}
	
	@Override
	public CertifiedProductTargetedUserDTO deleteCertifiedProductTargetedUser(Long id) throws EntityRetrievalException {
		
		CertifiedProductTargetedUserEntity curr = getEntityById(id);
		if(curr == null) {
			throw new EntityRetrievalException("Could not find mapping with id " + id);
		}
		curr.setDeleted(true);
		curr.setLastModifiedDate(new Date());
		curr.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(curr);
		entityManager.flush();

		return new CertifiedProductTargetedUserDTO(curr);
	}
	
	@Override
	public List<CertifiedProductTargetedUserDTO> getTargetedUsersByCertifiedProductId(Long certifiedProductId)
			throws EntityRetrievalException {
		List<CertifiedProductTargetedUserEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CertifiedProductTargetedUserDTO> dtos = new ArrayList<CertifiedProductTargetedUserDTO>();
		
		for (CertifiedProductTargetedUserEntity entity : entities){
			dtos.add(new CertifiedProductTargetedUserDTO(entity));
		}
		return dtos;
	}
	
	private CertifiedProductTargetedUserEntity getEntityById(Long id) throws EntityRetrievalException {
		CertifiedProductTargetedUserEntity entity = null;
		Query query = entityManager.createQuery( "SELECT tu from CertifiedProductTargetedUserEntity tu "
				+ "LEFT OUTER JOIN FETCH tu.targetedUser "
				+ "where (NOT tu.deleted = true) AND (certified_product_targeted_user_id = :entityid) ", 
				CertifiedProductTargetedUserEntity.class );

		query.setParameter("entityid", id);
		List<CertifiedProductTargetedUserEntity> result = query.getResultList();
		if(result.size() >= 1) {
			entity = result.get(0);
		} 
		return entity;
	}
	
	private List<CertifiedProductTargetedUserEntity> getEntitiesByCertifiedProductId(Long productId) throws EntityRetrievalException {
		Query query = entityManager.createQuery( "SELECT tu from CertifiedProductTargetedUserEntity tu "
				+ "LEFT OUTER JOIN FETCH tu.targetedUser "
				+ "where (NOT tu.deleted = true) AND (certified_product_id = :entityid) ", 
				CertifiedProductTargetedUserEntity.class );

		query.setParameter("entityid", productId);
		List<CertifiedProductTargetedUserEntity> result = query.getResultList();
		
		return result;
	}
	
}
