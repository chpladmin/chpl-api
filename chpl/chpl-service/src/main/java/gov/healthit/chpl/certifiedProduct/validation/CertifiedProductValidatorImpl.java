package gov.healthit.chpl.certifiedProduct.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
	@Autowired CertifiedProductDAO cpDao;
	@Autowired TestingLabDAO atlDao;
	@Autowired CertificationEditionDAO certEditionDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired DeveloperDAO developerDao;
	
	@Autowired
	protected CertificationResultRules certRules;
	
	Pattern urlRegex;
	
	public CertifiedProductValidatorImpl() {
		urlRegex = Pattern.compile(URL_PATTERN);
	}
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		//make sure the unique id is really uniqiue
		try {
			CertifiedProductDetailsDTO dup = cpDao.getByChplUniqueId(product.getUniqueId());
			if(dup != null) {
				product.getErrorMessages().add("The id " + product.getUniqueId() + " must be unique among all other certified products but one already exists with this ID.");
			}
		} catch(EntityRetrievalException ex) {}
		
		String uniqueId = product.getUniqueId();
		String[] uniqueIdParts = uniqueId.split("\\.");
		if(uniqueIdParts == null || uniqueIdParts.length != 9) {
			product.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have 9 parts separated by '.'");
			return;
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
			
			if(product.getTestingLabId() == null) {
				product.getErrorMessages().add("No testing lab was found matching the name '" + product.getTestingLabName() + "'");
			} else {
				TestingLabDTO testingLab = atlDao.getById(product.getTestingLabId());
				if(!testingLab.getTestingLabCode().equals(atlCode)) {
					product.getErrorMessages().add("The testing lab code provided does not match the assigned testing lab code '" + testingLab.getTestingLabCode() + "'.");
				}
			}
			
			CertificationBodyDTO certificationBody = null;
			if(product.getCertificationBodyId() == null) {
				product.getErrorMessages().add("No certification body was found matching the name '" + product.getCertificationBodyName() + "'.");
			} else {
				certificationBody = acbDao.getById(product.getCertificationBodyId());
				if(!certificationBody.getAcbCode().equals(acbCode)) {
					product.getErrorMessages().add("The ACB code provided does not match the assigned ACB code '" + certificationBody.getAcbCode() + "'.");
				}
			}
			
			if(product.getDeveloperId() != null) {
				DeveloperDTO developer = developerDao.getById(product.getDeveloperId());
				if(developer != null) {
					if(!developer.getDeveloperCode().equals(developerCode)) {
						product.getErrorMessages().add("The developer code '" + developerCode + "' does not match the assigned developer code for " + product.getDeveloperName() + ": '" + developer.getDeveloperCode() + "'.");
					}
					if(certificationBody != null) {
						DeveloperACBMapDTO mapping = developerDao.getTransparencyMapping(developer.getId(), certificationBody.getId());
						if(mapping != null) {
							//check transparency attestation and url for warnings
							if( (mapping.getTransparencyAttestation() == null && product.getTransparencyAttestation() != null) ||
								(mapping.getTransparencyAttestation() != null && product.getTransparencyAttestation() == null) || 
								(mapping.getTransparencyAttestation() != null && !mapping.getTransparencyAttestation().equals(product.getTransparencyAttestation()))) {
								product.getWarningMessages().add("The transparency attestation for the developer is different in the system than in the upload file. This value will be overwritten by what is in the upload file if you proceed.");
							}
						} else if(mapping == null && !StringUtils.isEmpty(product.getTransparencyAttestation())){
							product.getWarningMessages().add("The transparency attestation for the developer is different in the system than in the upload file. This value will be overwritten by what is in the upload file if you proceed.");
						}
					}
				}
			} else if(!developerCode.matches("X+")){
				DeveloperDTO developerByCode = developerDao.getByCode(developerCode);
				if(developerByCode == null) {
					product.getErrorMessages().add("The developer code " + developerCode + " does not match any developer in the system. New developers should use the code 'XXXX'.");
				} else {
					product.getErrorMessages().add("The developer code " + developerCode + " is for '" + developerByCode.getName() + "' which does not match the developer name in the upload file '" + product.getDeveloperName() + "'");
				}
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
		
		validateDemographics(product);
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
				for(PendingCertificationResultAdditionalSoftwareDTO asDto : cert.getAdditionalSoftware()) {
					if(!StringUtils.isEmpty(asDto.getChplId()) && asDto.getCertifiedProductId() == null) {
						product.getErrorMessages().add("No CHPL product was found matching additional software " + asDto.getChplId() + " for " + cert.getNumber());
					}
				}
			}
		}
	}
	@Override
	public void validate(CertifiedProductSearchDetails product) {
		//TODO: not sure if we should do the same validation here or not
	}
	
	protected void validateDemographics(PendingCertifiedProductDTO product) {
		if(product.getCertificationEditionId() == null && StringUtils.isEmpty(product.getCertificationEdition())) {
			product.getErrorMessages().add("Certification edition is required but was not found.");
		}
		if(StringUtils.isEmpty(product.getAcbCertificationId())) {
			product.getErrorMessages().add("CHPL certification ID is required but was not found.");
		}
		if(product.getCertificationDate() == null) {
			product.getErrorMessages().add("Certification date was not found.");
		} else if(product.getCertificationDate().getTime() > new Date().getTime()) {
			product.getErrorMessages().add("Certification date occurs in the future.");
		}
		if(product.getCertificationBodyId() == null) {
			product.getErrorMessages().add("ACB ID is required but was not found.");
		}
		
		if(StringUtils.isEmpty(product.getUniqueId())) {
			product.getErrorMessages().add("The product unique id is required.");
		}
		
		if(StringUtils.isEmpty(product.getDeveloperName())) {
			product.getErrorMessages().add("A developer name is required.");
		}
		
		if(StringUtils.isEmpty(product.getProductName())) {
			product.getErrorMessages().add("A product name is required.");
		}
		
		if(StringUtils.isEmpty(product.getProductVersion())) {
			product.getErrorMessages().add("A product version is required.");
		}
		
		if(product.getDeveloperAddress() != null) {
			if(StringUtils.isEmpty(product.getDeveloperAddress().getStreetLineOne())) {
				product.getErrorMessages().add("Developer street address is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperAddress().getCity())) {
				product.getErrorMessages().add("Developer city is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperAddress().getState())) {
				product.getErrorMessages().add("Developer state is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperAddress().getZipcode())) {
				product.getErrorMessages().add("Developer zip code is required.");
			}
		} else {
			if(StringUtils.isEmpty(product.getDeveloperStreetAddress())) {
				product.getErrorMessages().add("Developer street address is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperCity())) {
				product.getErrorMessages().add("Developer city is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperState())) {
				product.getErrorMessages().add("Developer state is required.");
			}
			
			if(StringUtils.isEmpty(product.getDeveloperZipCode())) {
				product.getErrorMessages().add("Developer zip code is required.");
			}
		}
		
		if(StringUtils.isEmpty(product.getDeveloperWebsite())) {
			product.getErrorMessages().add("Developer website is required.");
		}
		
		if(StringUtils.isEmpty(product.getDeveloperEmail())) {
			product.getErrorMessages().add("Developer contact email address is required.");
		}
		
		if(StringUtils.isEmpty(product.getDeveloperPhoneNumber())) {
			product.getErrorMessages().add("Developer contact phone number is required.");
		}
		
		if(StringUtils.isEmpty(product.getDeveloperContactName())) {
			product.getErrorMessages().add("Developer contact name is required.");
		}

		if(product.getIcs() == null) {
			product.getErrorMessages().add("ICS is required.");
		}
		
		if(!StringUtils.isEmpty(product.getTransparencyAttestationUrl()) && 
				urlRegex.matcher(product.getTransparencyAttestationUrl()).matches() == false) {
			product.getErrorMessages().add("Transparency attestation URL is not a valid URL format.");
		}
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getMeetsCriteria() == null) {
				product.getErrorMessages().add("0 or 1 is required to inidicate whether " + cert.getNumber() + " was met.");
			} else if(cert.getMeetsCriteria() == Boolean.TRUE) {
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == null) {
					product.getErrorMessages().add("GAP is required for certification " + cert.getNumber() + ".");
				}
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.SED) &&
						cert.getSed() == null) {
					product.getErrorMessages().add("SED is required for certification " + cert.getNumber() + ".");
				}
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION) &&
						(cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
					product.getErrorMessages().add("Test Procedures are required for certification " + cert.getNumber() + ".");
				}
			}
		}
	}
}
