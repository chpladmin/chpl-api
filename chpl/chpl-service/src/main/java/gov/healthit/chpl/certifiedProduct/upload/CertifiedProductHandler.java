package gov.healthit.chpl.certifiedProduct.upload;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("certifiedProductHandler")
public abstract class CertifiedProductHandler extends CertifiedProductUploadHandlerImpl {
	private static final Logger logger = LogManager.getLogger(CertifiedProductHandler.class);
	protected static final String PRACTICE_TYPE_AMBULATORY = "AMBULATORY";
	protected static final String PRACTICE_TYPE_INPATIENT = "INPATIENT";
	
	public abstract PendingCertifiedProductEntity handle();
	
	public Long getDefaultStatusId() {
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		if(statusDto != null) {
			return statusDto.getId();
		}
		return null;
	}
	
	/**
	 * look up the certification criteria by name and throw an error if we can't find it
	 * @param criterionName
	 * @param column
	 * @return
	 * @throws InvalidArgumentsException
	 */
	protected PendingCertificationCriterionEntity handleCertificationCriterion(String criterionName, int column) throws InvalidArgumentsException {
		CertificationCriterionEntity certEntity = certDao.getEntityByName(criterionName);
		if(certEntity == null) {
			throw new InvalidArgumentsException("Could not find a certification criterion matching " + criterionName);
		}
		
		PendingCertificationCriterionEntity result = new PendingCertificationCriterionEntity();		
		result.setMappedCriterion(certEntity);
		result.setMeetsCriteria(asBoolean(getRecord().get(column)));		
		return result;
	}
	
	protected boolean asBoolean(String value) {
		value = value.trim();
		
		if(StringUtils.isEmpty(value)) {
			return false;
		}
		
		boolean result = false;
		if("1".equals(value) || value.equalsIgnoreCase("true")) {
			result = true;
		}
		
		return result;
	}
}