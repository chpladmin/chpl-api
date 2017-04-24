package gov.healthit.chpl.validation.certifiedProduct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.DeveloperStatusType;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
	
	@Autowired CertifiedProductDAO cpDao;
	@Autowired CertifiedProductManager cpManager;
	@Autowired TestingLabDAO atlDao;
	@Autowired CertificationEditionDAO certEditionDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired DeveloperDAO developerDao;
	@Autowired TestToolDAO testToolDao;
	
	@Autowired
	protected CertificationResultRules certRules;
	
	protected Boolean hasIcsConflict;
	
	protected String icsCode;
	
	Pattern urlRegex;
	
	public CertifiedProductValidatorImpl() {
		urlRegex = Pattern.compile(URL_PATTERN);
	}

	@Override
	public boolean validateUniqueId(String chplProductNumber) {
		try {
			CertifiedProductDetailsDTO dup = cpDao.getByChplUniqueId(chplProductNumber);
			if(dup != null) {
				return false;
			}
		} catch(EntityRetrievalException ex) {}
		return true;
	}
	
	@Override
	public boolean validateProductCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String productCode = uniqueIdParts[PRODUCT_CODE_INDEX];
			if(StringUtils.isEmpty(productCode) || productCode.length() > 16 || !productCode.matches("^\\w+$")) {
				return false;
			}
		}
		return true;
	}
	
	private void updateChplProductNumber(CertifiedProductSearchDetails product, int productNumberIndex, String newValue) {
		String[] uniqueIdParts = product.getChplProductNumber().split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CHPL_PRODUCT_ID_PARTS) {
			String newChplProductCode = "";
			for(int idx = 0; idx < uniqueIdParts.length; idx++) {
				if(idx == productNumberIndex) {
					newChplProductCode += newValue;
				} else {
					newChplProductCode += uniqueIdParts[idx];
				}
				
				if(idx < uniqueIdParts.length-1) {
					newChplProductCode += ".";
				}
			}
			product.setChplProductNumber(newChplProductCode);
		}
	}
	
	@Override
	public void validate(PendingCertifiedProductDTO product) {
		String uniqueId = product.getUniqueId();
		String[] uniqueIdParts = uniqueId.split("\\.");
		if(uniqueIdParts == null || uniqueIdParts.length != CHPL_PRODUCT_ID_PARTS) {
			product.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have " + CHPL_PRODUCT_ID_PARTS + " parts separated by '.'");
			return;
		} 
		//validate that these pieces match up with data
		String editionCode = uniqueIdParts[EDITION_CODE_INDEX];
		String atlCode = uniqueIdParts[ATL_CODE_INDEX];
		String acbCode = uniqueIdParts[ACB_CODE_INDEX];
		String developerCode = uniqueIdParts[DEVELOPER_CODE_INDEX];
		String productCode = uniqueIdParts[PRODUCT_CODE_INDEX];
		String versionCode = uniqueIdParts[VERSION_CODE_INDEX];
		icsCode = uniqueIdParts[ICS_CODE_INDEX];
		String additionalSoftwareCode = uniqueIdParts[ADDITIONAL_SOFTWARE_CODE_INDEX];
		String certifiedDateCode = uniqueIdParts[CERTIFIED_DATE_CODE_INDEX];
		
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
				if(certificationBody != null && !certificationBody.getAcbCode().equals(acbCode)) {
					product.getErrorMessages().add("The ACB code provided does not match the assigned ACB code '" + certificationBody.getAcbCode() + "'.");
				}
			}
			
			if(product.getDeveloperId() != null && !developerCode.matches("X+")) {
				DeveloperDTO developer = developerDao.getById(product.getDeveloperId());
				if(developer != null) {
					DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
					if(mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
						product.getErrorMessages().add("The current status of the developer " + developer.getName() + " cannot be determined. A developer must be listed as Active in order to create certified products belongong to it.");
					} else if(!mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
						product.getErrorMessages().add("The developer " + developer.getName() + " has a status of " + mostRecentStatus.getStatus().getStatusName() + ". Certified products belonging to this developer cannot be created until its status returns to Active.");
					}
					
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
		
		if(!validateProductCodeCharacters(product.getUniqueId())) {
			product.getErrorMessages().add("The product code is required and must be 16 characters or less in length containing only the characters A-Z, a-z, 0-9, and _");
		}
		
		if(StringUtils.isEmpty(versionCode) || !versionCode.matches("^\\w+$")) {
			product.getErrorMessages().add("The version code is required and may only contain the characters A-Z, a-z, 0-9, and _");
		}
		
		if(StringUtils.isEmpty(icsCode) || !icsCode.matches("^\\d+$")) {
			product.getErrorMessages().add("The ICS code is required and may only contain the characters 0-9");
		}
			
		hasIcsConflict = false;
		if(icsCode.equals("0") && product.getIcs().equals(Boolean.TRUE)) {
			product.getErrorMessages().add("The unique id indicates the product does not have ICS but the ICS column in the upload file is true.");
			hasIcsConflict = true;
		} else if(!icsCode.equals("0") && product.getIcs().equals(Boolean.FALSE)) {
			product.getErrorMessages().add("The unique id indicates the product does have ICS but the ICS column in the upload file is false.");
			hasIcsConflict = true;
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
		
		//make sure the unique id is really uniqiue
		if(!validateUniqueId(product.getUniqueId())) {
			product.getErrorMessages().add("The id " + product.getUniqueId() + " must be unique among all other certified products but one already exists with this ID.");
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
		boolean productIdChanged = false;
		//if it's a new product, check the id parts
		String uniqueId = product.getChplProductNumber();
		String[] uniqueIdParts = uniqueId.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String productCode = uniqueIdParts[PRODUCT_CODE_INDEX];
			String versionCode = uniqueIdParts[VERSION_CODE_INDEX];
			icsCode = uniqueIdParts[ICS_CODE_INDEX];
			String additionalSoftwareCode = uniqueIdParts[ADDITIONAL_SOFTWARE_CODE_INDEX];
			String certifiedDateCode = uniqueIdParts[CERTIFIED_DATE_CODE_INDEX];
			
			try {
				if(product.getDeveloper() != null && product.getDeveloper().getDeveloperId() != null) {
					DeveloperDTO developer = developerDao.getById(product.getDeveloper().getDeveloperId());
					if(developer != null) {
						DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
						if(mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
							product.getErrorMessages().add("The current status of the developer " + developer.getName() + " cannot be determined. A developer must be listed as Active in order to create certified products belongong to it.");
						} else if(!mostRecentStatus.getStatus().getStatusName().equals(DeveloperStatusType.Active.toString())) {
							product.getErrorMessages().add("The developer " + developer.getName() + " has a status of " + mostRecentStatus.getStatus().getStatusName() + ". Certified products belonging to this developer cannot be created until its status returns to Active.");
						}
					} else {
						product.getErrorMessages().add("Could not find developer with id " + product.getDeveloper().getDeveloperId());
					}
				}
			} catch(EntityRetrievalException ex) {
				product.getErrorMessages().add("Could not find distinct developer with id " + product.getDeveloper().getDeveloperId());
			}
			
			if(!validateProductCodeCharacters(product.getChplProductNumber())) {
				product.getErrorMessages().add("The product code is required and must be 16 characters or less in length containing only the characters A-Z, a-z, 0-9, and _");
			}
			
			if(StringUtils.isEmpty(versionCode) || !versionCode.matches("^\\w+$")) {
				product.getErrorMessages().add("The version code is required and may only contain the characters A-Z, a-z, 0-9, and _");
			}
			
			if(StringUtils.isEmpty(icsCode) || !icsCode.matches("^\\d+$")) {
				product.getErrorMessages().add("The ICS code is required and may only contain the characters 0-9");
			}
			
			hasIcsConflict = false;
			if(icsCode.equals("0") && product.getIcs().equals(Boolean.TRUE)) {
				product.getErrorMessages().add("The unique id indicates the product does not have ICS but the value for Inherited Certification Status is true.");
				hasIcsConflict = true;
			} else if(!icsCode.equals("0") && product.getIcs().equals(Boolean.FALSE)) {
				product.getErrorMessages().add("The unique id indicates the product does have ICS but the value for Inherited Certification Status is false.");
				hasIcsConflict = true;
			}
			
			if(!additionalSoftwareCode.equals("0") && !additionalSoftwareCode.equals("1")) {
				product.getErrorMessages().add("The additional software part of the unique ID must be 0 or 1.");
			} else {
				boolean hasAS = false;
				for(CertificationResult cert : product.getCertificationResults()) {
					if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
						hasAS = true;
					}
				}
				String desiredAdditionalSoftwareCode = hasAS ? "1" : "0";
				if(!additionalSoftwareCode.equals(desiredAdditionalSoftwareCode)) {
					updateChplProductNumber(product, ADDITIONAL_SOFTWARE_CODE_INDEX, desiredAdditionalSoftwareCode);
					productIdChanged = true;
				}
			}
			
			SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
			idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date idDate = idDateFormat.parse(certifiedDateCode);
				if(product.getCertificationDate() == null || 
						idDate.getTime() != product.getCertificationDate().longValue()) {
					
					//change the certified date code to match the new certification date
					String desiredCertificationDateCode = idDateFormat.format(product.getCertificationDate());
					updateChplProductNumber(product, CERTIFIED_DATE_CODE_INDEX, desiredCertificationDateCode);
					productIdChanged = true;
				}
			} catch (ParseException pex) {
				product.getErrorMessages().add("Could not parse the certification date part of the product id: " + certifiedDateCode);
			}
		}
		
		if(productIdChanged) {
			//make sure the unique id is really unique - only check this if we know it changed
			//because if it hasn't changes there will be 1 product with its id
			if(!validateUniqueId(product.getChplProductNumber())) {
				product.getErrorMessages().add("The id " + product.getChplProductNumber() + " must be unique among all other certified products but one already exists with this ID.");
			}
		}
		
		validateDemographics(product);
		
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
				for(CertificationResultAdditionalSoftware asDto : cert.getAdditionalSoftware()) {
					if(asDto.getCertifiedProductId() == null && !StringUtils.isEmpty(asDto.getCertifiedProductNumber())) {
						try {
							boolean exists = cpManager.chplIdExists(asDto.getCertifiedProductNumber());
							if(!exists) {
								product.getErrorMessages().add("No CHPL product was found matching additional software " + asDto.getCertifiedProductNumber() + " for " + cert.getNumber());
							}
						} catch(Exception ex) {}
					}
				}
			}
		}
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
		
//		if(!StringUtils.isEmpty(product.getTransparencyAttestationUrl()) && 
//				urlRegex.matcher(product.getTransparencyAttestationUrl()).matches() == false) {
//			product.getErrorMessages().add("Transparency attestation URL is not a valid URL format.");
//		}
		
		for(PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
			if(cert.getMeetsCriteria() == null) {
				product.getErrorMessages().add("0 or 1 is required to inidicate whether " + cert.getNumber() + " was met.");
			} else if(cert.getMeetsCriteria() == Boolean.TRUE) {
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == null) {
					product.getErrorMessages().add("GAP is required for certification " + cert.getNumber() + ".");
				}
				
				boolean gapEligibleAndTrue = false;
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.getGap() == Boolean.TRUE) {
					gapEligibleAndTrue = true;
				}
				
				if(!gapEligibleAndTrue && 
						certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION) &&
						(cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
					product.getErrorMessages().add("Test Procedures are required for certification " + cert.getNumber() + ".");
				}
			}
		}
	}
	
	protected void validateDemographics(CertifiedProductSearchDetails product) {
		if(product.getCertificationEdition() == null || product.getCertificationEdition().get("id") == null) {
			product.getErrorMessages().add("Certification edition is required but was not found.");
		}
		if(StringUtils.isEmpty(product.getAcbCertificationId())) {
			product.getErrorMessages().add("CHPL certification ID is required but was not found.");
		}
		if(product.getCertificationDate() == null) {
			product.getErrorMessages().add("Certification date was not found.");
		} else if(product.getCertificationDate() > new Date().getTime()) {
			product.getErrorMessages().add("Certification date occurs in the future.");
		}
		if(product.getCertifyingBody() == null || product.getCertifyingBody().get("id") == null) {
			product.getErrorMessages().add("ACB ID is required but was not found.");
		}
		
		if(product.getDeveloper() == null) {
			product.getErrorMessages().add("A developer is required.");
		}
		
		if(product.getProduct() == null || StringUtils.isEmpty(product.getProduct().getName())) {
			product.getErrorMessages().add("A product name is required.");
		}
		
		if(product.getVersion() == null || StringUtils.isEmpty(product.getVersion().getVersion())) {
			product.getErrorMessages().add("A product version is required.");
		}
		
//		if(!StringUtils.isEmpty(product.getTransparencyAttestationUrl()) && 
//				urlRegex.matcher(product.getTransparencyAttestationUrl()).matches() == false) {
//			product.getErrorMessages().add("Transparency attestation URL is not a valid URL format.");
//		}
		
		for(CertificationResult cert : product.getCertificationResults()) {
			if(cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) &&
						cert.isGap() == null) {
					product.getErrorMessages().add("GAP is required for certification " + cert.getNumber() + ".");
				}
				//Jennifer asked to take out the test procedure validation for existing products
				//so that when users are on the edit screen, they are not required
				//to have test procedures for all certifications
//				if(certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE_VERSION) &&
//						(cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
//					product.getErrorMessages().add("Test Procedures are required for certification " + cert.getNumber() + ".");
//				}
			}
		}
	}
}
