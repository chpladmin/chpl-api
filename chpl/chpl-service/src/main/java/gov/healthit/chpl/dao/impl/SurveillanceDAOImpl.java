package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductInsertableOwnerEntity;
import gov.healthit.chpl.entity.SurveillanceRequirementTypeEntity;
import gov.healthit.chpl.entity.SurveillanceResultTypeEntity;
import gov.healthit.chpl.entity.SurveillanceTypeEntity;
import gov.healthit.chpl.entity.NonconformityStatusEntity;
import gov.healthit.chpl.entity.ProductActiveOwnerEntity;

@Repository("surveillanceDAO")
public class SurveillanceDAOImpl extends BaseDAOImpl implements SurveillanceDAO {
	private static final Logger logger = LogManager.getLogger(SurveillanceDAOImpl.class);
	
	public SurveillanceType findSurveillanceType(String type) {
		logger.debug("Searchig for surveillance type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceTypeEntity where name = :name and deleted <> true", 
				SurveillanceTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceTypeEntity> matches = query.getResultList();
		
		SurveillanceTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceType result = null;
		if(resultEntity != null) {
			result = new SurveillanceType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceRequirementType findSurveillanceRequirementType(String type) {
		logger.debug("Searchig for surveillance requirement type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceRequirementTypeEntity where name = :name and deleted <> true", 
				SurveillanceRequirementTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceRequirementTypeEntity> matches = query.getResultList();
		
		SurveillanceRequirementTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance requirement type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceRequirementType result = null;
		if(resultEntity != null) {
			result = new SurveillanceRequirementType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceResultType findSurveillanceResultType(String type) {
		logger.debug("Searchig for surveillance result type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from SurveillanceResultTypeEntity where name = :name and deleted <> true", 
				SurveillanceResultTypeEntity.class);
		query.setParameter("name", type);
		List<SurveillanceResultTypeEntity> matches = query.getResultList();
		
		SurveillanceResultTypeEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found surveillance result type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceResultType result = null;
		if(resultEntity != null) {
			result = new SurveillanceResultType();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
	
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type) {
		logger.debug("Searchig for nonconformity status type '" + type + "'.");
		Query query = entityManager.createQuery(
				"from NonconformityStatusEntity where name = :name and deleted <> true", 
				NonconformityStatusEntity.class);
		query.setParameter("name", type);
		List<NonconformityStatusEntity> matches = query.getResultList();
		
		NonconformityStatusEntity resultEntity = null;
		if(matches != null && matches.size() > 0) {
			resultEntity = matches.get(0);
			logger.debug("Found nonconformity status type '" + type + "' having id '" + resultEntity.getId() + "'.");
		}
		
		SurveillanceNonconformityStatus result = null;
		if(resultEntity != null) {
			result = new SurveillanceNonconformityStatus();
			result.setId(resultEntity.getId());
			result.setName(resultEntity.getName());
		}
		return result;
	}
}
