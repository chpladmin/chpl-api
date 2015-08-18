package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import javax.persistence.Query;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository(value="certificationBodyDAO")
public class CertificationBodyDAOImpl extends BaseDAOImpl implements CertificationBodyDAO {
	
	
	public void create(CertificationBodyDTO acb) throws EntityCreationException{
		
		CertificationBodyEntity acbEntity = null;
		try {
			if (acb.getId() != null){
				acbEntity = this.getEntityById(acb.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (acbEntity != null) {
			throw new EntityCreationException("A acb with this ID already exists.");
		} else {
			
			acbEntity = new CertificationBodyEntity();
			
			acbEntity.setId(acb.getId());
			acbEntity.setCreationDate(acb.getCreationDate());
			acbEntity.setDeleted(acb.getDeleted());
			//acbEntity.setLastModifiedDate(acb.getLastModifiedDate());
			acbEntity.setLastModifiedUser(Util.getCurrentUser().getId());
			acbEntity.setName(acb.getName());
			acbEntity.setWebsite(acb.getWebsite());
			
			create(acbEntity);	
		}
		
	}

	public void update(CertificationBodyDTO acb) throws EntityRetrievalException{
		
		CertificationBodyEntity acbEntity = getEntityById(acb.getId());		
		
		acbEntity.setId(acb.getId());
		acbEntity.setCreationDate(acb.getCreationDate());
		acbEntity.setDeleted(acb.getDeleted());
		//acbEntity.setLastModifiedDate(acb.getLastModifiedDate());
		acbEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		acbEntity.setName(acb.getName());
		acbEntity.setWebsite(acb.getWebsite());
		
		update(acbEntity);
		
	}
	
	public void delete(Long acbId){
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertificationBodyEntity SET deleted = true WHERE certified_product_id = :acbid");
		query.setParameter("acbid", acbId);
		query.executeUpdate();
		
	}
	
	public List<CertificationBodyDTO> findAll(){
		
		List<CertificationBodyEntity> entities = getAllEntities();
		List<CertificationBodyDTO> acbs = new ArrayList<>();
		
		for (CertificationBodyEntity entity : entities) {
			CertificationBodyDTO acb = new CertificationBodyDTO(entity);
			acbs.add(acb);
		}
		return acbs;
		
	}
	
	public CertificationBodyDTO getById(Long acbId) throws EntityRetrievalException{
		
		CertificationBodyEntity entity = getEntityById(acbId);
		CertificationBodyDTO dto = new CertificationBodyDTO(entity);
		return dto;
		
	}
	
	private void create(CertificationBodyEntity acb) {
		
		entityManager.persist(acb);
		
	}
	
	private void update(CertificationBodyEntity acb) {
		
		entityManager.merge(acb);	
	
	}
	
	private List<CertificationBodyEntity> getAllEntities() {
		
		List<CertificationBodyEntity> result = entityManager.createQuery( "from CertificationBodyEntity where (NOT deleted = true) ", CertificationBodyEntity.class).getResultList();
		return result;
		
	}
	
	private CertificationBodyEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		CertificationBodyEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertificationBodyEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CertificationBodyEntity.class );
		query.setParameter("entityid", entityId);
		List<CertificationBodyEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
}
