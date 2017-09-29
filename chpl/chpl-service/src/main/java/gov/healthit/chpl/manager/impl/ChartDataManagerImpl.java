package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.ClearAllCaches;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.statistics.ChartDataDAO;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.ChartDataEntity;
import gov.healthit.chpl.manager.ChartDataManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service("chartDataManager")
public class ChartDataManagerImpl implements ChartDataManager{
	
	@Autowired
	private ChartDataDAO chartDataDAO;
	
	@Transactional
	public ChartDataDTO create(ChartDataDTO cd) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		ChartDataDTO chartData = chartDataDAO.create(cd);
		
		return chartData;
	}
	
	@Transactional
	public ChartDataDTO update(ChartDataDTO cd) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		ChartDataDTO chartData = chartDataDAO.update(cd);
		
		return chartData;
	}
	
	@Transactional(readOnly = true)
	public List<ChartDataDTO> getAllData() {
		return chartDataDAO.findAllData();
	}
	
	@Transactional(readOnly = true)
	public List<ChartDataStatTypeDTO> getAllTypes() {
		return chartDataDAO.findAllTypes();
	}
	
	@Transactional(readOnly = true)
	public ChartDataDTO getById(Long id) throws EntityRetrievalException {
		return chartDataDAO.getById(id);
	}

}
