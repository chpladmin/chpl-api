package gov.healthit.chpl.manager.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.MeaningfulUseDAO;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

@Service
public class MeaningfulUseManagerImpl {
	private static final Logger logger = LogManager.getLogger(MeaningfulUseManagerImpl.class);
	
	@Autowired MeaningfulUseDAO meaningfulUseDao;
	
	public MeaningfulUseAccurateAsOfDTO getMeaningfulUseAccurateAsOf(){
		return meaningfulUseDao.getMeaningfulUseAccurateAsOf();
	}
	
	@Transactional(readOnly = false)
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CMS_STAFF')")
	public MeaningfulUseAccurateAsOfDTO updateMeaningfulUseAccurateAsOf(MeaningfulUseAccurateAsOfDTO meaningfulUseAccurateAsOfDTO){
		return meaningfulUseDao.updateAccurateAsOf(meaningfulUseAccurateAsOfDTO);
	}
}
