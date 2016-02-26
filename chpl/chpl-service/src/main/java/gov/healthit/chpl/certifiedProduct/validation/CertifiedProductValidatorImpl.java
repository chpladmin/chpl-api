package gov.healthit.chpl.certifiedProduct.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
	@Autowired TestingLabDAO atlDao;
	@Autowired CertificationEditionDAO certEditionDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired DeveloperDAO developerDao;
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		String uniqueId = product.getUniqueId();
		String[] uniqueIdParts = uniqueId.split("\\.");
		if(uniqueIdParts == null || uniqueIdParts.length != 9) {
			product.getErrorMessages().add("The unique CHPL ID provided must have 9 parts separated by '.'");
		}
		//validate that these pieces match up with data
		String editionCode = uniqueIdParts[0];
		String atlCode = uniqueIdParts[1];
		String acbCode = uniqueIdParts[2];
		String developerCode = uniqueIdParts[3];
		String icsCode = uniqueIdParts[6];
		String additionalSoftwareCode = uniqueIdParts[7];
		String certifiedDateCode = uniqueIdParts[8];
		
		try {
			CertificationEditionDTO certificationEdition = certEditionDao.getById(product.getCertificationEditionId());
			if(("2014".equals(certificationEdition.getYear()) && !"14".equals(editionCode)) ||
				("2015".equals(certificationEdition.getYear()) && !"15".equals(editionCode))) {
				product.getErrorMessages().add("The first part of the CHPL ID must match the certification year of the product.");
			}
			
			TestingLabDTO testingLab = atlDao.getById(product.getTestingLabId());
			if(!testingLab.getTestingLabCode().equals(atlCode)) {
				product.getErrorMessages().add("The testing lab code provided does not match the assigned testing lab code '" + testingLab.getTestingLabCode() + "'.");
			}
			
			CertificationBodyDTO certificationBody = acbDao.getById(product.getCertificationBodyId());
			if(!certificationBody.getAcbCode().equals(acbCode)) {
				product.getErrorMessages().add("The ACB code provided does not match the assigned ACB code '" + certificationBody.getAcbCode() + "'.");
			}
			
			DeveloperDTO developer = developerDao.getById(product.getDeveloperId());
			if(!developerCode.matches("X+") && 
				!developer.getDeveloperCode().equals(developerCode)) {
				product.getErrorMessages().add("The developer code provided does not match the assigned developer code '" + developer.getDeveloperCode() + "'.");
			}
		} catch(EntityRetrievalException ex) {
			product.getErrorMessages().add(ex.getMessage());
		}
		if(icsCode.equals("0") && product.getIcs().equals(Boolean.TRUE)) {
			product.getErrorMessages().add("The unique id indicates the product does not have ICS but the ICS column in the upload file is true.");
		} else if(!icsCode.equals("0") && product.getIcs().equals(Boolean.FALSE)) {
			product.getErrorMessages().add("The unique id indicates the product does have ICS but the ICS column in the upload file is false.");
		}
		if(additionalSoftwareCode.equals("0")) {
			boolean hasAS = false;
			for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
				if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
					hasAS = true;
				}
			}
			if(hasAS) {
				product.getErrorMessages().add("The unique id indicates the product does not have additional software but some is specified in the upload file.");
			}
		} else if(additionalSoftwareCode.equals("1")) {
			boolean hasAS = false;
			for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
				if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
					hasAS = true;
				}
			}
			if(!hasAS) {
				product.getErrorMessages().add("The unique id indicates the product has additional software but none is specified in the upload file.");
			}
		} else {
			product.getErrorMessages().add("The additional software part of the unique ID must be 0 or 1.");
		}
		SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
		try {
			Date idDate = idDateFormat.parse(certifiedDateCode);
			if(product.getCertificationDate() == null || 
					idDate.getTime() != product.getCertificationDate().getTime()) {
				product.getErrorMessages().add("The certification date provided in the unique id does not match the certification date in the upload file.");
			}
		} catch (ParseException pex) {
			product.getErrorMessages().add("Could not parse the certification date part of the product id: " + certifiedDateCode);
		}
	}
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		//TODO: not sure if we should do the same validation here or not
	}
	

}
