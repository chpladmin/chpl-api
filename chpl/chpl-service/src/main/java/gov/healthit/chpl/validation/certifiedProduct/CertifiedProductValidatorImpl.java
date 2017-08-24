package gov.healthit.chpl.validation.certifiedProduct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.CertificationResultRules;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
	@Autowired MessageSource messageSource;
	@Autowired CertifiedProductDAO cpDao;
	@Autowired CertifiedProductManager cpManager;
	@Autowired TestingLabDAO atlDao;
	@Autowired CertificationEditionDAO certEditionDao;
	@Autowired CertificationBodyDAO acbDao;
	@Autowired DeveloperDAO developerDao;
	@Autowired TestToolDAO testToolDao;
	@Autowired ListingGraphDAO inheritanceDao;
	
	@Autowired
	protected CertificationResultRules certRules;
	
	protected Boolean hasIcsConflict;
	
	protected Integer icsCodeInteger;
	
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
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
			if(StringUtils.isEmpty(productCode) || 
				!productCode.matches("^[a-zA-Z0-9_]{"+CertifiedProductDTO.PRODUCT_CODE_LENGTH+"}$")) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateVersionCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
			if(StringUtils.isEmpty(versionCode) || 
				!versionCode.matches("^[a-zA-Z0-9_]{"+CertifiedProductDTO.VERSION_CODE_LENGTH+"}$")) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateIcsCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			//validate that these pieces match up with data
			String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
			if(StringUtils.isEmpty(icsCode) || 
				!icsCode.matches("^[0-9]{"+CertifiedProductDTO.ICS_CODE_LENGTH+"}$")) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateAdditionalSoftwareCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			//validate that these pieces match up with data
			String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
			if(StringUtils.isEmpty(additionalSoftwareCode) || 
				!additionalSoftwareCode.matches("^0|1$")) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateCertifiedDateCodeCharacters(String chplProductNumber) {
		String[] uniqueIdParts = chplProductNumber.split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			//validate that these pieces match up with data
			String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
			if(StringUtils.isEmpty(certifiedDateCode) || 
				!certifiedDateCode.matches("^[0-9]{"+CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH+"}$")) {
				return false;
			}
		}
		return true;
	}
	
	private void updateChplProductNumber(CertifiedProductSearchDetails product, int productNumberIndex, String newValue) {
		String[] uniqueIdParts = product.getChplProductNumber().split("\\.");
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
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
		if(uniqueIdParts == null || uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			product.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have " + CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS + " parts separated by '.'");
			return;
		} 
		//validate that these pieces match up with data
		String editionCode = uniqueIdParts[CertifiedProductDTO.EDITION_CODE_INDEX];
		String atlCode = uniqueIdParts[CertifiedProductDTO.ATL_CODE_INDEX];
		String acbCode = uniqueIdParts[CertifiedProductDTO.ACB_CODE_INDEX];
		String developerCode = uniqueIdParts[CertifiedProductDTO.DEVELOPER_CODE_INDEX];
		String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
		String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
		String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
		String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
		
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
			product.getErrorMessages().add(
					String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.badProductCodeChars"), LocaleContextHolder.getLocale()), 
							CertifiedProductDTO.PRODUCT_CODE_LENGTH));
		}
		
		if(!validateVersionCodeCharacters(product.getUniqueId())) {
			product.getErrorMessages().add(
					String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.badVersionCodeChars"), LocaleContextHolder.getLocale()), 
							CertifiedProductDTO.VERSION_CODE_LENGTH));
		}
		
		hasIcsConflict = false;
		if(!validateIcsCodeCharacters(product.getUniqueId())) {
			product.getErrorMessages().add(
					String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.badIcsCodeChars"), LocaleContextHolder.getLocale()), 
							CertifiedProductDTO.ICS_CODE_LENGTH));
		} else {
			icsCodeInteger = new Integer(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
			if(icsCodeInteger != null) {
				if(icsCodeInteger.intValue() == 0 && product.getIcs().equals(Boolean.TRUE)) {
					product.getErrorMessages().add("The unique id indicates the product does not have ICS but the ICS column in the upload file is true.");
					hasIcsConflict = true;
				} else if(icsCodeInteger.intValue() > 0 && product.getIcs().equals(Boolean.FALSE)) {
					product.getErrorMessages().add("The unique id indicates the product does have ICS but the ICS column in the upload file is false.");
					hasIcsConflict = true;
				}
			}
		}
		
		if(!validateAdditionalSoftwareCodeCharacters(product.getUniqueId())) {
			product.getErrorMessages().add(
					String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.badAdditionalSoftwareCodeChars"), LocaleContextHolder.getLocale()), 
							CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
		} else {
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
			} 
		}
		
		if(!validateCertifiedDateCodeCharacters(product.getUniqueId())) {
			product.getErrorMessages().add(
					String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.badCertifiedDateCodeChars"), LocaleContextHolder.getLocale()), 
							CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
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
			if( (cert.getMeetsCriteria() == null || cert.getMeetsCriteria().booleanValue() == false)) {
				if(cert.getG1Success() != null && cert.getG1Success().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G1 Success"));
				}
				if(cert.getG2Success() != null && cert.getG2Success().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G2 Success"));
				}
				if(cert.getGap() != null && cert.getGap().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "GAP"));
				}
				if(cert.getSed() != null && cert.getSed().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "SED"));
				}
				if(!StringUtils.isEmpty(cert.getApiDocumentation())) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "API Documentation"));
				}
				if(!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "API Documentation"));
				}
				if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Additional Software"));
				}
				if(cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G1 Macra Measures"));
				}
				if(cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G2 Macra Measures"));
				}
				if(cert.getTestData() != null && cert.getTestData().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Data"));
				}
				if(cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Functionality"));
				}
				if(cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Procedures"));
				}
				if(cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Standards"));
				}
				if(cert.getTestTasks() != null && cert.getTestTasks().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Tasks"));
				}
				if(cert.getTestTools() != null && cert.getTestTools().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Tools"));
				}
				if(cert.getUcdProcesses() != null && cert.getUcdProcesses().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "UCD Processes"));
				}
			}
			
			if(!StringUtils.isEmpty(cert.getPrivacySecurityFramework())){
				String formattedPrivacyAndSecurityFramework = CertificationResult.formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
				PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept.getValue(formattedPrivacyAndSecurityFramework);
				if(foundPrivacyAndSecurityFramework == null){
					product.getErrorMessages().add("Certification " + cert.getNumber() + 
							" contains Privacy and Security Framework value '" + 
							formattedPrivacyAndSecurityFramework + "' which must match one of " +
							PrivacyAndSecurityFrameworkConcept.getFormattedValues());
				}
			}
			if(!StringUtils.isEmpty(cert.getPrivacySecurityFramework())){
				String formattedPrivacyAndSecurityFramework = CertificationResult.formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
				PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept.getValue(formattedPrivacyAndSecurityFramework);
				if(foundPrivacyAndSecurityFramework == null){
					product.getErrorMessages().add("Certification " + cert.getNumber() + 
							" contains Privacy and Security Framework value '" + 
							formattedPrivacyAndSecurityFramework + "' which must match one of " +
							PrivacyAndSecurityFrameworkConcept.getFormattedValues());
				}
			}
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
		if(uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
			
			//validate that these pieces match up with data
			String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
			String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
			
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
				product.getErrorMessages().add(
						String.format(messageSource.getMessage(
								new DefaultMessageSourceResolvable("listing.badProductCodeChars"), LocaleContextHolder.getLocale()), 
								CertifiedProductDTO.PRODUCT_CODE_LENGTH));
			}
			
			if(!validateVersionCodeCharacters(product.getChplProductNumber())) {
				product.getErrorMessages().add(
						String.format(messageSource.getMessage(
								new DefaultMessageSourceResolvable("listing.badVersionCodeChars"), LocaleContextHolder.getLocale()), 
								CertifiedProductDTO.VERSION_CODE_LENGTH));
			}
			
			hasIcsConflict = false;
			if(!validateIcsCodeCharacters(product.getChplProductNumber())) {
				product.getErrorMessages().add(
						String.format(messageSource.getMessage(
								new DefaultMessageSourceResolvable("listing.badIcsCodeChars"), LocaleContextHolder.getLocale()), 
								CertifiedProductDTO.ICS_CODE_LENGTH));
			} else {
				icsCodeInteger = new Integer(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
				if(icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
					if(product.getIcs() != null && product.getIcs().getParents() != null && 
							product.getIcs().getParents().size() > 0) {
						product.getErrorMessages().add("ICS Code is listed as 0 so no parents may be specified from which the listing inherits.");
					} 
						
					if(product.getIcs() != null && product.getIcs().getInherits() != null && 
							product.getIcs().getInherits().equals(Boolean.TRUE)) {
						product.getErrorMessages().add("The unique id indicates the product does not have ICS but the value for Inherited Certification Status is true.");
						hasIcsConflict = true;
					}
				} else if(product.getIcs() == null || product.getIcs().getInherits() == null ||
						product.getIcs().getInherits().equals(Boolean.FALSE) && 
						icsCodeInteger != null && icsCodeInteger.intValue() > 0) {
					product.getErrorMessages().add("The unique id indicates the product does have ICS but the value for Inherited Certification Status is false.");
					hasIcsConflict = true;
				}
			}
			
			if(!validateAdditionalSoftwareCodeCharacters(product.getChplProductNumber())) {
				product.getErrorMessages().add(
						String.format(messageSource.getMessage(
								new DefaultMessageSourceResolvable("listing.badAdditionalSoftwareCodeChars"), LocaleContextHolder.getLocale()), 
								CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
			} else {
				boolean hasAS = false;
				for(CertificationResult cert : product.getCertificationResults()) {
					if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
						hasAS = true;
					}
				}
				String desiredAdditionalSoftwareCode = hasAS ? "1" : "0";
				if(!additionalSoftwareCode.equals(desiredAdditionalSoftwareCode)) {
					updateChplProductNumber(product, CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX, desiredAdditionalSoftwareCode);
					productIdChanged = true;
				}
			}
			
			if(!validateCertifiedDateCodeCharacters(product.getChplProductNumber())) {
				product.getErrorMessages().add(
						String.format(messageSource.getMessage(
								new DefaultMessageSourceResolvable("listing.badCertifiedDateCodeChars"), LocaleContextHolder.getLocale()), 
								CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
			}
			SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
			idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			String desiredCertificationDateCode = idDateFormat.format(product.getCertificationDate());
			if(!certifiedDateCode.equals(desiredCertificationDateCode)) {
				//change the certified date code to match the new certification date
				updateChplProductNumber(product, CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX, desiredCertificationDateCode);
				productIdChanged = true;
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
			if( (cert.isSuccess() == null || cert.isSuccess().booleanValue() == false)) {
				if(cert.isG1Success() != null && cert.isG1Success().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G1 Success"));
				}
				if(cert.isG2Success() != null && cert.isG2Success().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G2 Success"));
				}
				if(cert.isGap() != null && cert.isGap().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "GAP"));
				}
				if(cert.isSed() != null && cert.isSed().booleanValue() == true) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "SED"));
				}
				if(!StringUtils.isEmpty(cert.getApiDocumentation())) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "API Documentation"));
				}
				if(!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "API Documentation"));
				}
				if(cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Additional Software"));
				}
				if(cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G1 Macra Measures"));
				}
				if(cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "G2 Macra Measures"));
				}
				if(cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Data"));
				}
				if(cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Functionality"));
				}
				if(cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Procedures"));
				}
				if(cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Standards"));
				}
				if(cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
					product.getWarningMessages().add(String.format(messageSource.getMessage(
							new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
							cert.getNumber(), "Test Tools"));
				}
				
				if(product.getSed() != null && product.getSed().getTestTasks() != null && product.getSed().getTestTasks().size() > 0) {
					for(TestTask tt : product.getSed().getTestTasks()) {
						for(CertificationCriterion ttCriteria : tt.getCriteria()) {
							if(ttCriteria.getNumber() != null && ttCriteria.getNumber().equals(cert.getNumber())) {
								product.getWarningMessages().add(String.format(messageSource.getMessage(
										new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
										cert.getNumber(), "Test Tasks"));
							} 
						}
					}
				}
				if(product.getSed() != null && product.getSed().getUcdProcesses() != null && product.getSed().getUcdProcesses().size() > 0) {
					for(UcdProcess ucd : product.getSed().getUcdProcesses()) {
						for(CertificationCriterion ucdCriteria : ucd.getCriteria()) {
							if(ucdCriteria.getNumber() != null && ucdCriteria.getNumber().equals(cert.getNumber())) {
								product.getWarningMessages().add(String.format(messageSource.getMessage(
										new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"), LocaleContextHolder.getLocale()), 
										cert.getNumber(), "UCD Processes"));
							} 
						}
					}
				}
			}
			
			if(!StringUtils.isEmpty(cert.getPrivacySecurityFramework())){
				String formattedPrivacyAndSecurityFramework = CertificationResult.formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
				PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept.getValue(formattedPrivacyAndSecurityFramework);
				if(foundPrivacyAndSecurityFramework == null){
					product.getErrorMessages().add("Certification " + cert.getNumber() + 
							" contains Privacy and Security Framework value '" + 
							formattedPrivacyAndSecurityFramework + "' which must match one of " +
							PrivacyAndSecurityFrameworkConcept.getFormattedValues());
				}
			}
			
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
