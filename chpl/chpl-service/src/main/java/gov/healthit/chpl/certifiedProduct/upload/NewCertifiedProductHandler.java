package gov.healthit.chpl.certifiedProduct.upload;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertificationCriterionEntity;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("newCertifiedProductHandler")
public abstract class NewCertifiedProductHandler extends CertifiedProductUploadHandlerImpl {
	private static final Logger logger = LogManager.getLogger(NewCertifiedProductHandler.class);
	
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