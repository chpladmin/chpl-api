package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceCertificationResultDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.SurveillanceCertificationResultEntity;
import gov.healthit.chpl.entity.SurveillanceEntity;

@Repository("surveillanceCertificationResultDAO")
public class SurveillanceCertificationResultDAOImpl extends BaseDAOImpl implements SurveillanceCertificationResultDAO {
	
	@Autowired SurveillanceDAO surveillanceDAO;
	@Autowired CertificationCriterionDAO certDao;
	
	@Override
	public SurveillanceCertificationResultDTO create(SurveillanceCertificationResultDTO toCreate) throws EntityCreationException,
		EntityRetrievalException {
		SurveillanceCertificationResultEntity entity = null;
		try {
			if(toCreate.getId() != null) {
				entity = getEntityById(toCreate.getId());
			}
		} catch(EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if(entity != null) {
			throw new EntityCreationException("An entity with this id already exists.");
		} else {
			SurveillanceEntity surveillance = surveillanceDAO.getEntityById(toCreate.getSurveillanceId());
			CertificationCriterionEntity cert = null;
			if(toCreate.getCertCriterion().getId() != null) {
				cert = certDao.getEntityById(toCreate.getCertCriterion().getId());
			} else if(!StringUtils.isEmpty(toCreate.getCertCriterion().getNumber())) {
				cert = certDao.getEntityByName(toCreate.getCertCriterion().getNumber());
			}
			
			if(cert == null) {
				throw new EntityCreationException("Cannot find a certification criterion with id " + toCreate.getCertCriterion().getId() + " or number " + toCreate.getCertCriterion().getNumber());
			}
			entity = new SurveillanceCertificationResultEntity();
			entity.setSurveillance(surveillance);
			entity.setCertCriterion(cert);
			entity.setNumSites(toCreate.getNumSites());
			entity.setPassRate(toCreate.getPassRate());
			entity.setResults(toCreate.getResults());
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			create(entity);
			return new SurveillanceCertificationResultDTO(entity);
		}
	}

	@Override
	public SurveillanceCertificationResultDTO update(SurveillanceCertificationResultDTO toUpdate) throws EntityRetrievalException {
		SurveillanceCertificationResultEntity entity = getEntityById(toUpdate.getId());
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + toUpdate.getId() + " does not exist.");
		}
		
		if(toUpdate.getSurveillanceId() != null) {
			SurveillanceEntity surveillance = surveillanceDAO.getEntityById(toUpdate.getSurveillanceId());
			entity.setSurveillance(surveillance);
		}
		if(toUpdate.getCertCriterion() != null) {
			CertificationCriterionEntity cert = certDao.getEntityById(toUpdate.getCertCriterion().getId());
			entity.setCertCriterion(cert);
		}
		
		entity.setNumSites(toUpdate.getNumSites());
		entity.setPassRate(toUpdate.getPassRate());
		entity.setResults(toUpdate.getResults());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
		return new SurveillanceCertificationResultDTO(entity);
	}
	
	@Override
	public SurveillanceCertificationResultDTO getById(Long id) throws EntityRetrievalException {
		SurveillanceCertificationResultDTO dto = null;
		SurveillanceCertificationResultEntity entity = getEntityById(id);
		if(entity != null) {
			dto = new SurveillanceCertificationResultDTO(entity);
		}
		return dto;
	}


	@Override
	public List<SurveillanceCertificationResultDTO> getAllForSurveillance(Long surveillanceId) {
		List<SurveillanceCertificationResultEntity> entities = getEntitiesBySurveillance(surveillanceId);
		List<SurveillanceCertificationResultDTO> dtos = new ArrayList<SurveillanceCertificationResultDTO>();
		
		for(SurveillanceCertificationResultEntity entity : entities) {
			SurveillanceCertificationResultDTO dto = new SurveillanceCertificationResultDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		SurveillanceCertificationResultEntity entity = getEntityById(id);
		entity.setDeleted(true);
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
	}
	
	private void create(SurveillanceCertificationResultEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(SurveillanceCertificationResultEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private SurveillanceCertificationResultEntity getEntityById(Long id) throws EntityRetrievalException {
		
		SurveillanceCertificationResultEntity entity = null;
			
		Query query = entityManager.createQuery( "from SurveillanceCertificationResultEntity sCert "
				+ "JOIN FETCH sCert.surveillance s " 
				+ "JOIN FETCH sCert.certCriterion cert " 
				+ "where (NOT sCert.deleted = true) AND (sCert.id = :entityid) ", SurveillanceCertificationResultEntity.class );
		query.setParameter("entityid", id);
		List<SurveillanceCertificationResultEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate surveillance certification id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private List<SurveillanceCertificationResultEntity> getEntitiesBySurveillance(Long id) {
		
		Query query = entityManager.createQuery( "from SurveillanceCertificationResultEntity sCert "
				+ "JOIN FETCH sCert.surveillance s "
				+ "JOIN FETCH sCert.certCriterion cert "
				+ "where (NOT sCert.deleted = true) AND (s.id = :surveillance_id) ", SurveillanceCertificationResultEntity.class );
		query.setParameter("surveillance_id", id);
		List<SurveillanceCertificationResultEntity> result = query.getResultList();
		
		return result;
	}	
}