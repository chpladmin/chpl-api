package gov.healthit.chpl.validation.certifiedProduct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.AccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingTestParticipantDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.FuzzyChoicesManager;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ValidationUtils;

public class CertifiedProductValidatorImpl implements CertifiedProductValidator {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductValidatorImpl.class);

    @Autowired
    MessageSource messageSource;
    @Autowired
    CertifiedProductDAO cpDao;
    @Autowired
    CertifiedProductManager cpManager;
    @Autowired
    TestingLabDAO atlDao;
    @Autowired
    CertificationEditionDAO certEditionDao;
    @Autowired
    CertificationBodyDAO acbDao;
    @Autowired
    DeveloperDAO developerDao;
    @Autowired
    TestToolDAO testToolDao;
    @Autowired
    ListingGraphDAO inheritanceDao;
    @Autowired
    UcdProcessDAO ucdDao;
    @Autowired
    QmsStandardDAO qmsDao;
    @Autowired
    AccessibilityStandardDAO accStdDao;
    @Autowired
    FuzzyChoicesManager fuzzyChoicesManager;

    @Autowired
    protected CertificationResultRules certRules;

    protected Boolean hasIcsConflict;

    protected Integer icsCodeInteger;

    Pattern urlRegex;

    public CertifiedProductValidatorImpl() {
        urlRegex = Pattern.compile(URL_PATTERN);
    }
    
    public void checkField(Object product, Object field, String errorField){
    	if(field instanceof Long){
    		Long fieldCasted = (Long) field;
    		if(fieldCasted.toString().length() > getMaxLength("maxLength." + errorField)){
    			if(product instanceof PendingCertifiedProductDTO){
    				PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) product;
    				productCasted.getErrorMessages().add(getErrorMessage("listing." + errorField + ".maxlength"));
    			}else{
    				CertifiedProductSearchDetails productCasted = (CertifiedProductSearchDetails) product;
    				productCasted.getErrorMessages().add(getErrorMessage("listing." + errorField + ".maxlength"));
    			}
    		}
    	}else if(field instanceof String){
    		String fieldCasted = (String) field;
    		if(fieldCasted.length() > getMaxLength("maxLength." + errorField)){
    			if(product instanceof PendingCertifiedProductDTO){
    				PendingCertifiedProductDTO productCasted = (PendingCertifiedProductDTO) product;
    				productCasted.getErrorMessages().add(getErrorMessage("listing." + errorField + ".maxlength"));
    			}else{
    				CertifiedProductSearchDetails productCasted = (CertifiedProductSearchDetails) product;
    				productCasted.getErrorMessages().add(getErrorMessage("listing." + errorField + ".maxlength"));
    			}
    		}
    	}
    }
    
    public int getMaxLength(String field){
    	return Integer.parseInt(String.format(
    			messageSource.getMessage(new DefaultMessageSourceResolvable(field),
    			LocaleContextHolder.getLocale())));
    }
    
    public String getErrorMessage(String errorField){
    		return String.format(
    				messageSource.getMessage(new DefaultMessageSourceResolvable(errorField),
    				LocaleContextHolder.getLocale()));
    }

    public String getErrorMessage(String errorField, String input){
    	return String.format(messageSource.getMessage(
                new DefaultMessageSourceResolvable(errorField),
                LocaleContextHolder.getLocale()), input);
    }

    @Override
    public boolean validateUniqueId(String chplProductNumber) {
        try {
            CertifiedProductDetailsDTO dup = cpDao.getByChplUniqueId(chplProductNumber);
            if (dup != null) {
                return false;
            }
        } catch (final EntityRetrievalException ex) {
        }
        return true;
    }

    @Override
    public boolean validateProductCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
            if (StringUtils.isEmpty(productCode)
                    || !productCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.PRODUCT_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateVersionCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
            if (StringUtils.isEmpty(versionCode)
                    || !versionCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.VERSION_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateIcsCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.ICS_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateAdditionalSoftwareCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            if (StringUtils.isEmpty(additionalSoftwareCode) || !additionalSoftwareCode.matches("^0|1$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateCertifiedDateCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
            if (StringUtils.isEmpty(certifiedDateCode)
                    || !certifiedDateCode.matches("^[0-9]{" + CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    private void updateChplProductNumber(CertifiedProductSearchDetails product, int productNumberIndex,
            String newValue) {
        String[] uniqueIdParts = product.getChplProductNumber().split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            String newChplProductCode = "";
            for (int idx = 0; idx < uniqueIdParts.length; idx++) {
                if (idx == productNumberIndex) {
                    newChplProductCode += newValue;
                } else {
                    newChplProductCode += uniqueIdParts[idx];
                }

                if (idx < uniqueIdParts.length - 1) {
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
        if (uniqueIdParts == null || uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            product.getErrorMessages().add("The unique CHPL ID '" + uniqueId + "' must have "
                    + CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS + " parts separated by '.'");
            return;
        }
        // validate that these pieces match up with data
        String editionCode = uniqueIdParts[CertifiedProductDTO.EDITION_CODE_INDEX];
        String atlCode = uniqueIdParts[CertifiedProductDTO.ATL_CODE_INDEX];
        String acbCode = uniqueIdParts[CertifiedProductDTO.ACB_CODE_INDEX];
        String developerCode = uniqueIdParts[CertifiedProductDTO.DEVELOPER_CODE_INDEX];
        String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
        String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
        String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
        String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
        
        if(product.getCertificationCriterion() != null && !product.getCertificationCriterion().isEmpty()){
        	for(PendingCertificationResultDTO cert : product.getCertificationCriterion()){
        		if(cert.getUcdProcesses() != null && !cert.getUcdProcesses().isEmpty()){
        			for(PendingCertificationResultUcdProcessDTO ucd : cert.getUcdProcesses()) {
        			    String origUcdProcessName = ucd.getUcdProcessName();
        				String topChoice = fuzzyChoicesManager.getTopFuzzyChoice(origUcdProcessName, FuzzyType.UCD_PROCESS);
        				if(topChoice != null && !origUcdProcessName.equals(topChoice)) {
        				    UcdProcessDTO fuzzyMatchedUcd = null;
        				    try {
        				        fuzzyMatchedUcd = ucdDao.findOrCreate(null, topChoice);
        				    } catch(EntityCreationException ex) {
        				        LOGGER.error("Could not insert ucd process " + topChoice, ex);
        				    }
        				    
        				    if(fuzzyMatchedUcd != null) {
        				        ucd.setUcdProcessId(fuzzyMatchedUcd.getId());
                                ucd.setUcdProcessName(fuzzyMatchedUcd.getName());
                                
                                String warningMsg = String.format(
                                        messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.fuzzyMatch"),
                                                LocaleContextHolder.getLocale()), FuzzyType.UCD_PROCESS.fuzzyType(), 
                                                cert.getNumber(), origUcdProcessName, topChoice);
                                product.getWarningMessages().add(warningMsg);
        				    }
        				}
        			}
        		}
        	}
        }
        
        for(PendingCertifiedProductQmsStandardDTO qms : product.getQmsStandards()){
            String origQmsName = qms.getName();
            String topChoice = fuzzyChoicesManager.getTopFuzzyChoice(origQmsName, FuzzyType.QMS_STANDARD);
            if(topChoice != null && !origQmsName.equals(topChoice)) {
                QmsStandardDTO fuzzyMatchedQms = null;
                try {
                    fuzzyMatchedQms = qmsDao.findOrCreate(null, topChoice);
                } catch(EntityCreationException ex) {
                    LOGGER.error("Could not insert qms standard " + topChoice, ex);
                }
                
                if(fuzzyMatchedQms != null) {
                    qms.setQmsStandardId(fuzzyMatchedQms.getId());
                    qms.setName(fuzzyMatchedQms.getName());
                    
                    String warningMsg = String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.fuzzyMatch"),
                                    LocaleContextHolder.getLocale()), FuzzyType.QMS_STANDARD.fuzzyType(), 
                                    origQmsName, topChoice);
                    product.getWarningMessages().add(warningMsg);
                }
            }
        }
        
        for(PendingCertifiedProductAccessibilityStandardDTO access : product.getAccessibilityStandards()){
            String origAccStd = access.getName();
            String topChoice = fuzzyChoicesManager.getTopFuzzyChoice(origAccStd, FuzzyType.ACCESSIBILITY_STANDARD);
            if(topChoice != null && !origAccStd.equals(topChoice)) {
                AccessibilityStandardDTO fuzzyMatchedAccStd = null;
                try {
                    fuzzyMatchedAccStd = accStdDao.findOrCreate(null, topChoice);
                } catch(EntityCreationException ex) {
                    LOGGER.error("Could not insert accessibility standard " + topChoice, ex);
                }
                
                if(fuzzyMatchedAccStd != null) {
                    access.setAccessibilityStandardId(fuzzyMatchedAccStd.getId());
                    access.setName(fuzzyMatchedAccStd.getName());
                    
                    String warningMsg = String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.fuzzyMatch"),
                                    LocaleContextHolder.getLocale()), FuzzyType.ACCESSIBILITY_STANDARD.fuzzyType(), 
                                    origAccStd, topChoice);
                    product.getWarningMessages().add(warningMsg);
                }
            }
        }

        try {
            CertificationEditionDTO certificationEdition = certEditionDao.getById(product.getCertificationEditionId());
            if (("2014".equals(certificationEdition.getYear()) && !"14".equals(editionCode))
                    || ("2015".equals(certificationEdition.getYear()) && !"15".equals(editionCode))) {
                product.getErrorMessages()
                        .add("The first part of the CHPL ID must match the certification year of the product.");
            }

            if (product.getTestingLabId() == null) {
                product.getErrorMessages()
                        .add("No testing lab was found matching the name '" + product.getTestingLabName() + "'");
            } else {
                TestingLabDTO testingLab = atlDao.getById(product.getTestingLabId());
                if (!testingLab.getTestingLabCode().equals(atlCode)) {
                    product.getErrorMessages()
                            .add("The testing lab code provided does not match the assigned testing lab code '"
                                    + testingLab.getTestingLabCode() + "'.");
                }
            }

            CertificationBodyDTO certificationBody = null;
            if (product.getCertificationBodyId() == null) {
                product.getErrorMessages().add("No certification body was found matching the name '"
                        + product.getCertificationBodyName() + "'.");
            } else {
                certificationBody = acbDao.getById(product.getCertificationBodyId());
                if (certificationBody != null && !certificationBody.getAcbCode().equals(acbCode)) {
                    product.getErrorMessages().add("The ACB code provided does not match the assigned ACB code '"
                            + certificationBody.getAcbCode() + "'.");
                }
            }

            if (product.getDeveloperId() != null && !developerCode.matches("X+")) {
                DeveloperDTO developer = developerDao.getById(product.getDeveloperId());
                if (developer != null) {
                    DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                    if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                        product.getErrorMessages().add("The current status of the developer " + developer.getName()
                                + " cannot be determined. A developer must be listed as Active in order to create certified products belongong to it.");
                    } else if (!mostRecentStatus.getStatus().getStatusName()
                            .equals(DeveloperStatusType.Active.toString())) {
                        product.getErrorMessages().add("The developer " + developer.getName() + " has a status of "
                                + mostRecentStatus.getStatus().getStatusName()
                                + ". Certified products belonging to this developer cannot be created until its status returns to Active.");
                    }

                    if (!developer.getDeveloperCode().equals(developerCode)) {
                        product.getErrorMessages()
                                .add("The developer code '" + developerCode
                                        + "' does not match the assigned developer code for "
                                        + product.getDeveloperName() + ": '" + developer.getDeveloperCode() + "'.");
                    }
                    if (certificationBody != null) {
                        DeveloperACBMapDTO mapping = developerDao.getTransparencyMapping(developer.getId(),
                                certificationBody.getId());
                        if (mapping != null) {
                            // check transparency attestation and url for
                            // warnings
                            if ((mapping.getTransparencyAttestation() == null
                                    && product.getTransparencyAttestation() != null)
                                    || (mapping.getTransparencyAttestation() != null
                                            && product.getTransparencyAttestation() == null)
                                    || (mapping.getTransparencyAttestation() != null
                                            && !mapping.getTransparencyAttestation()
                                                    .equals(product.getTransparencyAttestation()))) {
                                product.getWarningMessages().add(String.format(
                                        messageSource.getMessage(new DefaultMessageSourceResolvable("transparencyAttestation.save"),
                                                LocaleContextHolder.getLocale())));
                            }
                        } else if (mapping == null && !StringUtils.isEmpty(product.getTransparencyAttestation())) {
                            product.getWarningMessages().add(String.format(
                                    messageSource.getMessage(new DefaultMessageSourceResolvable("transparencyAttestation.save"),
                                            LocaleContextHolder.getLocale())));
                        }
                    }
                }
            } else if (!developerCode.matches("X+")) {
                DeveloperDTO developerByCode = developerDao.getByCode(developerCode);
                if (developerByCode == null) {
                    product.getErrorMessages().add("The developer code " + developerCode
                            + " does not match any developer in the system. New developers should use the code 'XXXX'.");
                } else {
                    product.getErrorMessages()
                            .add("The developer code " + developerCode + " is for '" + developerByCode.getName()
                                    + "' which does not match the developer name in the upload file '"
                                    + product.getDeveloperName() + "'");
                }
            }
        } catch (final EntityRetrievalException ex) {
            product.getErrorMessages().add(ex.getMessage());
        }

        if (!validateProductCodeCharacters(product.getUniqueId())) {
            product.getErrorMessages()
                    .add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badProductCodeChars"),
                                    LocaleContextHolder.getLocale()),
                            CertifiedProductDTO.PRODUCT_CODE_LENGTH));
        }

        if (!validateVersionCodeCharacters(product.getUniqueId())) {
            product.getErrorMessages()
                    .add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badVersionCodeChars"),
                                    LocaleContextHolder.getLocale()),
                            CertifiedProductDTO.VERSION_CODE_LENGTH));
        }

        hasIcsConflict = false;
        if (!validateIcsCodeCharacters(product.getUniqueId())) {
            product.getErrorMessages()
                    .add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badIcsCodeChars"),
                                    LocaleContextHolder.getLocale()),
                            CertifiedProductDTO.ICS_CODE_LENGTH));
        } else {
            icsCodeInteger = new Integer(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
            if (icsCodeInteger != null) {
                if (icsCodeInteger.intValue() == 0 && product.getIcs() != null && product.getIcs().equals(Boolean.TRUE)) {
                    product.getErrorMessages().add(
                            "The unique id indicates the product does not have ICS but the ICS column in the upload file is true.");
                    hasIcsConflict = true;
                } else if (icsCodeInteger.intValue() > 0 && product.getIcs() != null && product.getIcs().equals(Boolean.FALSE)) {
                    product.getErrorMessages().add(
                            "The unique id indicates the product does have ICS but the ICS column in the upload file is false.");
                    hasIcsConflict = true;
                }
            }
        }

        if (!validateAdditionalSoftwareCodeCharacters(product.getUniqueId())) {
            product.getErrorMessages()
                    .add(String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable("listing.badAdditionalSoftwareCodeChars"),
                            LocaleContextHolder.getLocale()), CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
        } else {
            if (additionalSoftwareCode.equals("0")) {
                boolean hasAS = false;
                for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
                    if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                        hasAS = true;
                    }
                }
                if (hasAS) {
                    product.getErrorMessages().add(
                            "The unique id indicates the product does not have additional software but some is specified in the upload file.");
                }
            } else if (additionalSoftwareCode.equals("1")) {
                boolean hasAS = false;
                for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
                    if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                        hasAS = true;
                    }
                }
                if (!hasAS) {
                    product.getErrorMessages().add(
                            "The unique id indicates the product has additional software but none is specified in the upload file.");
                }
            }
        }

        if (!validateCertifiedDateCodeCharacters(product.getUniqueId())) {
            product.getErrorMessages()
                    .add(String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable("listing.badCertifiedDateCodeChars"),
                            LocaleContextHolder.getLocale()), CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
        }
        SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
        try {
            Date idDate = idDateFormat.parse(certifiedDateCode);
            if (product.getCertificationDate() == null
                    || idDate.getTime() != product.getCertificationDate().getTime()) {
                product.getErrorMessages().add(
                        "The certification date provided in the unique id does not match the certification date in the upload file.");
            }
        } catch (final ParseException pex) {
            product.getErrorMessages()
                    .add("Could not parse the certification date part of the product id: " + certifiedDateCode);
        }

        // make sure the unique id is really uniqiue
        if (!validateUniqueId(product.getUniqueId())) {
            product.getErrorMessages().add("The id " + product.getUniqueId()
                    + " must be unique among all other certified products but one already exists with this ID.");
        }

        validateDemographics(product);
        weirdCharacterCheck(product);
        
        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if ((cert.getMeetsCriteria() == null || cert.getMeetsCriteria().booleanValue() == false)) {
                if (cert.getG1Success() != null && cert.getG1Success().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G1 Success"));
                }
                if (cert.getG2Success() != null && cert.getG2Success().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G2 Success"));
                }
                if (cert.getGap() != null && cert.getGap().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "GAP"));
                }
                if (cert.getSed() != null && cert.getSed().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "SED"));
                }
                if (!StringUtils.isEmpty(cert.getApiDocumentation())) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "API Documentation"));
                }
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    product.getWarningMessages()
                            .add(String.format(
                                    messageSource.getMessage(
                                            new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                            LocaleContextHolder.getLocale()),
                                    cert.getNumber(), "Privacy and Security Framework"));
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Additional Software"));
                }
                if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G1 Macra Measures"));
                }
                if (cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G2 Macra Measures"));
                }
                if (cert.getTestData() != null && cert.getTestData().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Data"));
                }
                if (cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Functionality"));
                }
                if (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Procedures"));
                }
                if (cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Standards"));
                }
                if (cert.getTestTasks() != null && cert.getTestTasks().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Tasks"));
                }
                if (cert.getTestTools() != null && cert.getTestTools().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Tools"));
                }
                if (cert.getUcdProcesses() != null && cert.getUcdProcesses().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "UCD Processes"));
                }
            }

            if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                String formattedPrivacyAndSecurityFramework = CertificationResult
                        .formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
                PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                        .getValue(formattedPrivacyAndSecurityFramework);
                if (foundPrivacyAndSecurityFramework == null) {
                    product.getErrorMessages().add("Certification " + cert.getNumber()
                            + " contains Privacy and Security Framework value '" + formattedPrivacyAndSecurityFramework
                            + "' which must match one of " + PrivacyAndSecurityFrameworkConcept.getFormattedValues());
                }
            }
            if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                for (PendingCertificationResultAdditionalSoftwareDTO asDto : cert.getAdditionalSoftware()) {
                    if (!StringUtils.isEmpty(asDto.getChplId()) && asDto.getCertifiedProductId() == null) {
                        product.getErrorMessages().add("No CHPL product was found matching additional software "
                                + asDto.getChplId() + " for " + cert.getNumber());
                    }
                }
            }
        }
    }

    @Override
    public void validate(CertifiedProductSearchDetails product) {
        boolean productIdChanged = false;
        // if it's a new product, check the id parts
        String uniqueId = product.getChplProductNumber();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];

            try {
                if (product.getDeveloper() != null && product.getDeveloper().getDeveloperId() != null) {
                    DeveloperDTO developer = developerDao.getById(product.getDeveloper().getDeveloperId());
                    if (developer != null) {
                        DeveloperStatusEventDTO mostRecentStatus = developer.getStatus();
                        if (mostRecentStatus == null || mostRecentStatus.getStatus() == null) {
                            product.getErrorMessages().add("The current status of the developer " + developer.getName()
                                    + " cannot be determined. A developer must be listed as Active in order to create certified products belongong to it.");
                        } else if (!mostRecentStatus.getStatus().getStatusName()
                                .equals(DeveloperStatusType.Active.toString())) {
                            product.getErrorMessages().add("The developer " + developer.getName() + " has a status of "
                                    + mostRecentStatus.getStatus().getStatusName()
                                    + ". Certified products belonging to this developer cannot be created until its status returns to Active.");
                        }
                    } else {
                        product.getErrorMessages()
                                .add("Could not find developer with id " + product.getDeveloper().getDeveloperId());
                    }
                }
            } catch (final EntityRetrievalException ex) {
                product.getErrorMessages()
                        .add("Could not find distinct developer with id " + product.getDeveloper().getDeveloperId());
            }

            if (!validateProductCodeCharacters(product.getChplProductNumber())) {
                product.getErrorMessages()
                        .add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.badProductCodeChars"),
                                LocaleContextHolder.getLocale()), CertifiedProductDTO.PRODUCT_CODE_LENGTH));
            }

            if (!validateVersionCodeCharacters(product.getChplProductNumber())) {
                product.getErrorMessages()
                        .add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.badVersionCodeChars"),
                                LocaleContextHolder.getLocale()), CertifiedProductDTO.VERSION_CODE_LENGTH));
            }

            hasIcsConflict = false;
            if (!validateIcsCodeCharacters(product.getChplProductNumber())) {
                product.getErrorMessages()
                        .add(String.format(
                                messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badIcsCodeChars"),
                                        LocaleContextHolder.getLocale()),
                                CertifiedProductDTO.ICS_CODE_LENGTH));
            } else {
                icsCodeInteger = new Integer(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
                if (icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
                    if (product.getIcs() != null && product.getIcs().getParents() != null
                            && product.getIcs().getParents().size() > 0) {
                        product.getErrorMessages().add(getErrorMessage("listing.ics00"));
                    }

                    if (product.getIcs() != null && product.getIcs().getInherits() != null
                            && product.getIcs().getInherits().equals(Boolean.TRUE)) {
                        product.getErrorMessages().add(
                                "The unique id indicates the product does not have ICS but the value for Inherited Certification Status is true.");
                        hasIcsConflict = true;
                    }
                } else if (product.getIcs() == null || product.getIcs().getInherits() == null
                        || product.getIcs().getInherits().equals(Boolean.FALSE) && icsCodeInteger != null
                                && icsCodeInteger.intValue() > 0) {
                    product.getErrorMessages().add(
                            "The unique id indicates the product does have ICS but the value for Inherited Certification Status is false.");
                    hasIcsConflict = true;
                }
            }

            if (!validateAdditionalSoftwareCodeCharacters(product.getChplProductNumber())) {
                product.getErrorMessages()
                        .add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.badAdditionalSoftwareCodeChars"),
                                LocaleContextHolder.getLocale()), CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
            } else {
                boolean hasAS = false;
                for (CertificationResult cert : product.getCertificationResults()) {
                    if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                        hasAS = true;
                    }
                }
                String desiredAdditionalSoftwareCode = hasAS ? "1" : "0";
                if (!additionalSoftwareCode.equals(desiredAdditionalSoftwareCode)) {
                    updateChplProductNumber(product, CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX,
                            desiredAdditionalSoftwareCode);
                    productIdChanged = true;
                }
            }

            if (!validateCertifiedDateCodeCharacters(product.getChplProductNumber())) {
                product.getErrorMessages()
                        .add(String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("listing.badCertifiedDateCodeChars"),
                                LocaleContextHolder.getLocale()), CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
            }
            SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
            idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String desiredCertificationDateCode = idDateFormat.format(product.getCertificationDate());
            if (!certifiedDateCode.equals(desiredCertificationDateCode)) {
                // change the certified date code to match the new certification
                // date
                updateChplProductNumber(product, CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX,
                        desiredCertificationDateCode);
                productIdChanged = true;
            }
        }

        if (productIdChanged) {
            // make sure the unique id is really unique - only check this if we
            // know it changed
            // because if it hasn't changes there will be 1 product with its id
            if (!validateUniqueId(product.getChplProductNumber())) {
                product.getErrorMessages().add("The id " + product.getChplProductNumber()
                        + " must be unique among all other certified products but one already exists with this ID.");
            }
        }

        validateDemographics(product);
        weirdCharacterCheck(product);
        
        for (CertificationResult cert : product.getCertificationResults()) {
            if ((cert.isSuccess() == null || cert.isSuccess().booleanValue() == false)) {
                if (cert.isG1Success() != null && cert.isG1Success().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G1 Success"));
                }
                if (cert.isG2Success() != null && cert.isG2Success().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G2 Success"));
                }
                if (cert.isGap() != null && cert.isGap().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "GAP"));
                }
                if (cert.isSed() != null && cert.isSed().booleanValue() == true) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "SED"));
                }
                if (!StringUtils.isEmpty(cert.getApiDocumentation())) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "API Documentation"));
                }
                if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "API Documentation"));
                }
                if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Additional Software"));
                }
                if (cert.getG1MacraMeasures() != null && cert.getG1MacraMeasures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G1 Macra Measures"));
                }
                if (cert.getG2MacraMeasures() != null && cert.getG2MacraMeasures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "G2 Macra Measures"));
                }
                if (cert.getTestDataUsed() != null && cert.getTestDataUsed().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Data"));
                }
                if (cert.getTestFunctionality() != null && cert.getTestFunctionality().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Functionality"));
                }
                if (cert.getTestProcedures() != null && cert.getTestProcedures().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Procedures"));
                }
                if (cert.getTestStandards() != null && cert.getTestStandards().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Standards"));
                }
                if (cert.getTestToolsUsed() != null && cert.getTestToolsUsed().size() > 0) {
                    product.getWarningMessages()
                            .add(String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.criteria.falseCriteriaHasData"),
                                    LocaleContextHolder.getLocale()), cert.getNumber(), "Test Tools"));
                }

                if (product.getSed() != null && product.getSed().getTestTasks() != null
                        && product.getSed().getTestTasks().size() > 0) {
                    for (TestTask tt : product.getSed().getTestTasks()) {
                        for (CertificationCriterion ttCriteria : tt.getCriteria()) {
                            if (ttCriteria.getNumber() != null && ttCriteria.getNumber().equals(cert.getNumber())) {
                                product.getWarningMessages().add(String.format(
                                        messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.falseCriteriaHasData"),
                                                LocaleContextHolder.getLocale()),
                                        cert.getNumber(), "Test Tasks"));
                            }
                        }
                    }
                }
                if (product.getSed() != null && product.getSed().getUcdProcesses() != null
                        && product.getSed().getUcdProcesses().size() > 0) {
                    for (UcdProcess ucd : product.getSed().getUcdProcesses()) {
                        for (CertificationCriterion ucdCriteria : ucd.getCriteria()) {
                            if (ucdCriteria.getNumber() != null && ucdCriteria.getNumber().equals(cert.getNumber())) {
                                product.getWarningMessages().add(String.format(
                                        messageSource.getMessage(
                                                new DefaultMessageSourceResolvable(
                                                        "listing.criteria.falseCriteriaHasData"),
                                                LocaleContextHolder.getLocale()),
                                        cert.getNumber(), "UCD Processes"));
                            }
                        }
                    }
                }
            }

            if (!StringUtils.isEmpty(cert.getPrivacySecurityFramework())) {
                String formattedPrivacyAndSecurityFramework = CertificationResult
                        .formatPrivacyAndSecurityFramework(cert.getPrivacySecurityFramework());
                PrivacyAndSecurityFrameworkConcept foundPrivacyAndSecurityFramework = PrivacyAndSecurityFrameworkConcept
                        .getValue(formattedPrivacyAndSecurityFramework);
                if (foundPrivacyAndSecurityFramework == null) {
                    product.getErrorMessages().add("Certification " + cert.getNumber()
                            + " contains Privacy and Security Framework value '" + formattedPrivacyAndSecurityFramework
                            + "' which must match one of " + PrivacyAndSecurityFrameworkConcept.getFormattedValues());
                }
            }

            if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                for (CertificationResultAdditionalSoftware asDto : cert.getAdditionalSoftware()) {
                    if (asDto.getCertifiedProductId() == null
                            && !StringUtils.isEmpty(asDto.getCertifiedProductNumber())) {
                        try {
                            boolean exists = cpManager.chplIdExists(asDto.getCertifiedProductNumber());
                            if (!exists) {
                                product.getErrorMessages().add("No CHPL product was found matching additional software "
                                        + asDto.getCertifiedProductNumber() + " for " + cert.getNumber());
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
    }

    protected void validateDemographics(PendingCertifiedProductDTO product) {
        if (product.getCertificationEditionId() == null && StringUtils.isEmpty(product.getCertificationEdition())) {
            product.getErrorMessages().add("Certification edition is required but was not found.");
        }
        checkField(product, product.getCertificationEditionId(), "certificationEdition");
        checkField(product, product.getAcbCertificationId(), "acbCertificationId");
        if (product.getCertificationDate() == null) {
            product.getErrorMessages().add("Certification date was not found.");
        } else if (product.getCertificationDate().getTime() > new Date().getTime()) {
            product.getErrorMessages().add("Certification date occurs in the future.");
        }
        if (product.getCertificationBodyId() == null) {
            product.getErrorMessages().add("ONC-ACB is required but was not found.");
        }
        checkField(product, product.getCertificationBodyId(), "certifyingAcb");
        if (StringUtils.isEmpty(product.getUniqueId())) {
            product.getErrorMessages().add("The product unique id is required.");
        }
        checkField(product, product.getUniqueId(), "uniqueCHPLId");
        if (StringUtils.isEmpty(product.getDeveloperName())) {
            product.getErrorMessages().add("A developer name is required.");
        }
        checkField(product, product.getDeveloperName(), "vendorName");
        if (StringUtils.isEmpty(product.getProductName())) {
            product.getErrorMessages().add("A product name is required.");
        }
        checkField(product, product.getProductName(), "productName");
        if (StringUtils.isEmpty(product.getProductVersion())) {
            product.getErrorMessages().add("A product version is required.");
        }
        checkField(product, product.getProductVersion(), "productVersion");
        if (product.getDeveloperAddress() != null) {
            if (StringUtils.isEmpty(product.getDeveloperAddress().getStreetLineOne())) {
                product.getErrorMessages().add("Developer street address is required.");
            }
            checkField(product, product.getDeveloperAddress().getStreetLineOne(), "vendorStreetAddress");
            checkField(product, product.getDeveloperAddress().getStreetLineTwo(), "vendorStreetAddressTwo");
            if (StringUtils.isEmpty(product.getDeveloperAddress().getCity())) {
                product.getErrorMessages().add("Developer city is required.");
            }
            checkField(product, product.getDeveloperAddress().getCity(), "vendorCity");
            if (StringUtils.isEmpty(product.getDeveloperAddress().getState())) {
                product.getErrorMessages().add("Developer state is required.");
            }
            checkField(product, product.getDeveloperAddress().getState(), "vendorState");
            if (StringUtils.isEmpty(product.getDeveloperAddress().getZipcode())) {
                product.getErrorMessages().add("Developer zip code is required.");
            }
            checkField(product, product.getDeveloperAddress().getZipcode(), "vendorZip");
        } else {
            if (StringUtils.isEmpty(product.getDeveloperStreetAddress())) {
                product.getErrorMessages().add("Developer street address is required.");
            }
            checkField(product, product.getDeveloperStreetAddress(), "vendorStreetAddress");
            if (StringUtils.isEmpty(product.getDeveloperCity())) {
                product.getErrorMessages().add("Developer city is required.");
            }
            checkField(product, product.getDeveloperCity(), "vendorCity");
            if (StringUtils.isEmpty(product.getDeveloperState())) {
                product.getErrorMessages().add("Developer state is required.");
            }
            checkField(product, product.getDeveloperState(), "vendorState");
            if (StringUtils.isEmpty(product.getDeveloperZipCode())) {
                product.getErrorMessages().add("Developer zip code is required.");
            }
            checkField(product, product.getDeveloperZipCode(), "vendorZip");
        }

        if (StringUtils.isEmpty(product.getDeveloperWebsite())) {
            product.getErrorMessages().add("Developer website is required.");
        }
        checkField(product, product.getDeveloperWebsite(), "vendorWebsite");
        if (StringUtils.isEmpty(product.getDeveloperEmail())) {
            product.getErrorMessages().add("Developer contact email address is required.");
        }
        checkField(product, product.getDeveloperEmail(), "vendorEmail");
        if (StringUtils.isEmpty(product.getDeveloperPhoneNumber())) {
            product.getErrorMessages().add("Developer contact phone number is required.");
        }
        checkField(product, product.getDeveloperPhoneNumber(), "vendorPhone");
        if (StringUtils.isEmpty(product.getDeveloperContactName())) {
            product.getErrorMessages().add("Developer contact name is required.");
        }
        checkField(product, product.getDeveloperContactName(), "vendorContactName");

        // if(!StringUtils.isEmpty(product.getTransparencyAttestationUrl()) &&
        // urlRegex.matcher(product.getTransparencyAttestationUrl()).matches()
        // == false) {
        // product.getErrorMessages().add("Transparency attestation URL is not a
        // valid URL format.");
        // }

        for (PendingCertificationResultDTO cert : product.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() == null) {
                product.getErrorMessages()
                        .add("0 or 1 is required to inidicate whether " + cert.getNumber() + " was met.");
            } else if (cert.getMeetsCriteria() == Boolean.TRUE) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) && cert.getGap() == null) {
                    product.getErrorMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.missingGap"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                }

                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                    && cert.getGap() == Boolean.TRUE) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                    && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                    && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    product.getErrorMessages().add(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("listing.criteria.missingTestProcedure"),
                        LocaleContextHolder.getLocale()),
                        cert.getNumber()));
                }
            }
        }
    }

    protected void validateDemographics(CertifiedProductSearchDetails product) {
        if (product.getCertificationEdition() == null || product.getCertificationEdition().get("id") == null) {
            product.getErrorMessages().add("Certification edition is required but was not found.");
        }else{
        	checkField(product, product.getCertificationEdition().get("id"), "certificationEdition");
        }
        if (StringUtils.isEmpty(product.getAcbCertificationId())) {
            product.getWarningMessages().add("CHPL certification ID was not found.");
        }
        if (product.getCertificationDate() == null) {
            product.getErrorMessages().add("Certification date was not found.");
        } else if (product.getCertificationDate() > new Date().getTime()) {
            product.getErrorMessages().add("Certification date occurs in the future.");
        }
        if (product.getDeveloper() == null) {
            product.getErrorMessages().add("A developer is required.");
        }
        if (product.getProduct() == null || StringUtils.isEmpty(product.getProduct().getName())) {
            product.getErrorMessages().add("A product name is required.");
        }else{
            checkField(product, product.getProduct().getName(), "productName");
        }
        if (product.getVersion() == null || StringUtils.isEmpty(product.getVersion().getVersion())) {
            product.getErrorMessages().add("A product version is required.");
        }else{
            checkField(product, product.getVersion().getVersion(), "productVersion");
        }
        // if(!StringUtils.isEmpty(product.getTransparencyAttestationUrl()) &&
        // urlRegex.matcher(product.getTransparencyAttestationUrl()).matches()
        // == false) {
        // product.getErrorMessages().add("Transparency attestation URL is not a
        // valid URL format.");
        // }

        //check if the oldest status for the updated listing is 'Active'
        CertificationStatusEvent  earliestStatus = product.getOldestStatus();
        if(earliestStatus != null) {
            CertificationStatus earliestStatusInUpdate = product.getOldestStatus().getStatus();
            if(earliestStatusInUpdate == null || 
                    !CertificationStatusType.Active.getName().equals(earliestStatusInUpdate.getName())) {
                String msg = String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable(
                                "listing.firstStatusNotActive"),
                        LocaleContextHolder.getLocale()), CertificationStatusType.Active.getName());
                product.getErrorMessages().add(msg);
            }
        } else {
            String msg = String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable(
                            "listing.noStatusProvided"), LocaleContextHolder.getLocale()));
            product.getErrorMessages().add(msg);
        }
        
        for (CertificationResult cert : product.getCertificationResults()) {
            if (cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) && cert.isGap() == null) {
                    product.getErrorMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.missingGap"),
                                    LocaleContextHolder.getLocale()), cert.getNumber()));
                }
                // Jennifer asked to take out the test procedure validation for
                // existing products
                // so that when users are on the edit screen, they are not
                // required
                // to have test procedures for all certifications
                // if(certRules.hasCertOption(cert.getNumber(),
                // CertificationResultRules.TEST_PROCEDURE_VERSION) &&
                // (cert.getTestProcedures() == null ||
                // cert.getTestProcedures().size() == 0)) {
                // product.getErrorMessages().add("Test Procedures are required
                // for certification " + cert.getNumber() + ".");
                // }
            }
        }
    }
    
    protected void weirdCharacterCheck(PendingCertifiedProductDTO listing) {
        //check all string fields at the listing level
        addListingWarningIfNotValid(listing, listing.getAcbCertificationId(), 
                "ACB Certification ID '" + listing.getAcbCertificationId() + "'");
        addListingWarningIfNotValid(listing, listing.getCertificationBodyName(), 
                "ACB Name '" + listing.getCertificationBodyName() + "'");
        addListingWarningIfNotValid(listing, listing.getCertificationEdition(), 
                "Certification Edition '" + listing.getCertificationEdition() + "'");
        if(listing.getDeveloperAddress() != null) {
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getStreetLineOne(), 
                    "Developer's Street Address (Line 1) '" + listing.getDeveloperAddress().getStreetLineOne() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getStreetLineTwo(), 
                    "Developer's Street Address (Line 2) '" + listing.getDeveloperAddress().getStreetLineTwo() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getCity(), 
                    "Developer's City '" + listing.getDeveloperAddress().getCity() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getState(), 
                    "Developer's State '" + listing.getDeveloperAddress().getState() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getZipcode(), 
                    "Developer's Zip Code '" + listing.getDeveloperAddress().getZipcode() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperAddress().getCountry(), 
                    "Developer's Country '" + listing.getDeveloperAddress().getCountry() + "'");
        } else {
            addListingWarningIfNotValid(listing, listing.getDeveloperStreetAddress(), 
                    "Developer's Street Address '" + listing.getDeveloperStreetAddress() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperCity(), 
                    "Developer's City '" + listing.getDeveloperCity() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperState(), 
                    "Developer's State '" + listing.getDeveloperState() + "'");
            addListingWarningIfNotValid(listing, listing.getDeveloperZipCode(), 
                    "Developer's Zip Code '" + listing.getDeveloperZipCode() + "'");
        }
        addListingWarningIfNotValid(listing, listing.getDeveloperContactName(), 
                "Developer's Contact Name '" + listing.getDeveloperContactName() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperEmail(), 
                "Developer's Email Address '" + listing.getDeveloperEmail() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperPhoneNumber(), 
                "Developer's Phone Number '" + listing.getDeveloperPhoneNumber() + "'");
        addListingWarningIfNotValid(listing, listing.getDeveloperWebsite(), 
                "Developer's Website '" + listing.getDeveloperWebsite() + "'");
        addListingWarningIfNotValid(listing, listing.getPracticeType(), 
                "Practice Type '" + listing.getPracticeType() + "'");
        addListingWarningIfNotValid(listing, listing.getProductClassificationName(), 
                "Product Classification '" + listing.getProductClassificationName() + "'");
        addListingWarningIfNotValid(listing, listing.getProductName(), 
                "Product Name '" + listing.getProductName() + "'");
        addListingWarningIfNotValid(listing, listing.getProductVersion(), 
                "Version '" + listing.getProductVersion() + "'");
        addListingWarningIfNotValid(listing, listing.getReportFileLocation(), 
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getSedIntendedUserDescription(), 
                "SED Intended User Description '" + listing.getSedIntendedUserDescription() + "'");
        addListingWarningIfNotValid(listing, listing.getSedReportFileLocation(), 
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getTestingLabName(), 
                "Testing Lab Name '" + listing.getTestingLabName() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestation(), 
                "Transparency Attestation '" + listing.getTransparencyAttestation() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestationUrl(), 
                "Transparency Attestation URL '" + listing.getTransparencyAttestationUrl() + "'");
        
        //users can add to accessibility standards so check these
        for(PendingCertifiedProductAccessibilityStandardDTO accStd : listing.getAccessibilityStandards()) {
            addListingWarningIfNotValid(listing, accStd.getName(), "Accessibility Standard '" + accStd.getName() + "'");
        }
        
        //users can add to qms standards so check these
        for(PendingCertifiedProductQmsStandardDTO qmsStd : listing.getQmsStandards()) {
            addListingWarningIfNotValid(listing, qmsStd.getName(), "QMS Standard '" + qmsStd.getName() + "'");
            addListingWarningIfNotValid(listing, qmsStd.getModification(), "QMS Modification '" + qmsStd.getModification() + "'");
        }
        
        //users can add to targeted users so check these
        for(PendingCertifiedProductTargetedUserDTO tu : listing.getTargetedUsers()) {
            addListingWarningIfNotValid(listing, tu.getName(), "Targeted User '" + tu.getName() + "'");
        }
        
        //check all criteria fields
        for(PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if(cert.getMeetsCriteria() != null && cert.getMeetsCriteria() == Boolean.TRUE) {
                addCriteriaWarningIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                if(cert.getAdditionalSoftware() != null) {
                    for(PendingCertificationResultAdditionalSoftwareDTO addSoft : cert.getAdditionalSoftware()) {
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getName(), "Additional Software Name '" + addSoft.getName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getVersion(), "Additional Software Version '" + addSoft.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getJustification(), "Additional Software Justification '" + addSoft.getJustification() + "'");
                    }
                }
                if(cert.getTestData() != null) {
                    for(PendingCertificationResultTestDataDTO testData : cert.getTestData()) {
                        //not checking test data name because it has to match one of the existing names
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testData.getVersion(), "Test Data Version '" + testData.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testData.getAlteration(), "Test Data Alteration '" + testData.getAlteration() + "'");
                    }
                }
                
                //not checking test functionality name because it has to match one of the existing options

                if(cert.getTestProcedures() != null) {
                    for(PendingCertificationResultTestProcedureDTO testProc : cert.getTestProcedures()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testProc.getVersion(), "Test Procedure Version '" + testProc.getVersion() + "'");
                    }
                }
                if(cert.getTestStandards() != null) {
                    for(PendingCertificationResultTestStandardDTO testStd : cert.getTestStandards()) {
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testStd.getName(), "Test Standard Name '" + testStd.getName() + "'");
                    }
                }
                if(cert.getTestTools() != null) {
                    for(PendingCertificationResultTestToolDTO testTool : cert.getTestTools()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testTool.getVersion(), "Test Tool Version '" + testTool.getVersion() + "'");
                    }
                }
                if(cert.getTestTasks() != null) {
                    for(PendingCertificationResultTestTaskDTO crTestTask : cert.getTestTasks()) {
                        PendingTestTaskDTO testTask = crTestTask.getPendingTestTask();
                        if(testTask != null) {
                            //not checking anything converted to a number
                            addCriteriaWarningIfNotValid(listing, cert, 
                                    testTask.getDescription() , "Test Task Description '" + testTask.getDescription() + "'");
                            addCriteriaWarningIfNotValid(listing, cert, 
                                    testTask.getTaskRatingScale() , "Test Task Rating Scale '" + testTask.getTaskRatingScale() + "'");
                        }
                        if(crTestTask.getTaskParticipants() != null) {
                            for(PendingCertificationResultTestTaskParticipantDTO crPart : crTestTask.getTaskParticipants()) {
                                PendingTestParticipantDTO part = crPart.getTestParticipant();
                                if(part != null) {
                                    //not checking age range or education level because they have to map
                                    //to existing values. also not checking anything converted to a number
                                    addCriteriaWarningIfNotValid(listing, cert, 
                                            part.getAssistiveTechnologyNeeds() , 
                                            "Participant Assistive Technology Needs '" + part.getAssistiveTechnologyNeeds() + "'");
                                    addCriteriaWarningIfNotValid(listing, cert, 
                                            part.getGender() , 
                                            "Participant Gender '" + part.getGender() + "'");
                                    addCriteriaWarningIfNotValid(listing, cert, 
                                            part.getOccupation() , 
                                            "Participant Occupation '" + part.getOccupation() + "'");
                                }
                            }
                        }
                    }
                }
                if(cert.getUcdProcesses() != null) {
                    for(PendingCertificationResultUcdProcessDTO ucd : cert.getUcdProcesses()) {
                        addCriteriaWarningIfNotValid(listing, cert, 
                                ucd.getUcdProcessName(), "UCD Process Name '" + ucd.getUcdProcessName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                ucd.getUcdProcessDetails(), "UCD Process Details '" + ucd.getUcdProcessDetails() + "'");
                    }
                }
            }
        }
    }
    
    protected void weirdCharacterCheck(CertifiedProductSearchDetails listing) {
        //check all string fields at the listing level
        addListingWarningIfNotValid(listing, listing.getAcbCertificationId(), 
                "ACB Certification ID '" + listing.getAcbCertificationId() + "'");
        if(listing.getCertifyingBody() != null && listing.getCertifyingBody().get("name") != null) {
            addListingWarningIfNotValid(listing, listing.getCertifyingBody().get("name").toString(), 
                    "ACB Name '" + listing.getCertifyingBody().get("name").toString() + "'");
        }
        if(listing.getCertificationEdition() != null && listing.getCertificationEdition().get("name") != null) {
            addListingWarningIfNotValid(listing, listing.getCertificationEdition().get("name").toString(), 
                    "Certification Edition '" + listing.getCertificationEdition().get("name").toString() + "'");
        }
        addListingWarningIfNotValid(listing, listing.getProductAdditionalSoftware(), 
                "Listing-level Additional Software '" + listing.getProductAdditionalSoftware() + "'");

        if(listing.getDeveloper() != null && listing.getDeveloper().getAddress() != null) {
            Address address = listing.getDeveloper().getAddress();
            addListingWarningIfNotValid(listing, address.getLine1(), 
                    "Developer's Street Address (Line 1) '" + address.getLine1() + "'");
            addListingWarningIfNotValid(listing, address.getLine2(), 
                    "Developer's Street Address (Line 2) '" + address.getLine2() + "'");
            addListingWarningIfNotValid(listing, address.getCity(), 
                    "Developer's City '" + address.getCity() + "'");
            addListingWarningIfNotValid(listing, address.getState(), 
                    "Developer's State '" + address.getState() + "'");
            addListingWarningIfNotValid(listing, address.getZipcode(), 
                    "Developer's Zip Code '" + address.getZipcode() + "'");
            addListingWarningIfNotValid(listing, address.getCountry(), 
                    "Developer's Country  '" + address.getCountry() + "'");
        }
        
        if(listing.getDeveloper() != null && listing.getDeveloper().getContact() != null) {
            Contact contact = listing.getDeveloper().getContact();
            addListingWarningIfNotValid(listing, contact.getFirstName(), 
                    "Developer Contact's First Name '" + contact.getFirstName() + "'");
            addListingWarningIfNotValid(listing, contact.getLastName(), 
                    "Developer Contact's Last Name '" + contact.getLastName() + "'");
            addListingWarningIfNotValid(listing, contact.getEmail(), 
                    "Developer Contact's Email Address '" + contact.getEmail() + "'");
            addListingWarningIfNotValid(listing, contact.getPhoneNumber(), 
                    "Developer Contact's Phone Number '" + contact.getPhoneNumber() + "'");
            addListingWarningIfNotValid(listing, contact.getTitle(), 
                    "Developer Contact's Title '" + contact.getTitle() + "'");
        }
        
        if(listing.getDeveloper() != null) {
            addListingWarningIfNotValid(listing, listing.getDeveloper().getWebsite(), 
                    "Developer's Website '" + listing.getDeveloper().getWebsite() + "'");
        }

        if(listing.getPracticeType() != null && listing.getPracticeType().get("name") != null) {
            addListingWarningIfNotValid(listing, listing.getPracticeType().get("name").toString(), 
                    "Practice Type '" + listing.getPracticeType().get("name").toString() + "'");
        }
        if(listing.getClassificationType() != null && listing.getClassificationType().get("name") != null) {
            addListingWarningIfNotValid(listing, listing.getClassificationType().get("name").toString(), 
                    "Product Classification '" + listing.getClassificationType().get("name").toString() + "'");
        }
        if(listing.getProduct() != null) {
            addListingWarningIfNotValid(listing, listing.getProduct().getName(), 
                    "Product Name '" + listing.getProduct().getName() + "'");
        }
        if(listing.getVersion() != null) {
            addListingWarningIfNotValid(listing, listing.getVersion().getVersion(), 
                    "Version Name '" + listing.getVersion().getVersion() + "'");
        }
        
        if(listing.getTestingLab() != null && listing.getTestingLab().get("name") != null) {
            addListingWarningIfNotValid(listing, listing.getTestingLab().get("name").toString(), 
                    "Testing Lab Name '" + listing.getTestingLab().get("name").toString() + "'");
        }

        addListingWarningIfNotValid(listing, listing.getReportFileLocation(), 
                "Report File Location '" + listing.getReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getSedIntendedUserDescription(),
                "SED Intended User Description '" + listing.getSedIntendedUserDescription() + "'");
        addListingWarningIfNotValid(listing, listing.getSedReportFileLocation(), 
                "SED Report File Location '" + listing.getSedReportFileLocation() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestation(), 
                "Transparency Attestation '" + listing.getTransparencyAttestation() + "'");
        addListingWarningIfNotValid(listing, listing.getTransparencyAttestationUrl(), 
                "Transparency Attestation URL '" + listing.getTransparencyAttestationUrl() + "'");
        
        //users can add to accessibility standards so check these
        for(CertifiedProductAccessibilityStandard accStd : listing.getAccessibilityStandards()) {
            addListingWarningIfNotValid(listing, accStd.getAccessibilityStandardName(), 
                    "Accessibility Standard '" + accStd.getAccessibilityStandardName() + "'");
        }
        
        //users can add to qms standards so check these
        for(CertifiedProductQmsStandard qmsStd : listing.getQmsStandards()) {
            addListingWarningIfNotValid(listing, qmsStd.getQmsStandardName(), 
                    "QMS Standard '" + qmsStd.getQmsStandardName() + "'");
            addListingWarningIfNotValid(listing, qmsStd.getQmsModification(), 
                    "QMS Modification '" + qmsStd.getQmsModification() + "'");
        }
        
        //users can add to targeted users so check these
        for(CertifiedProductTargetedUser tu : listing.getTargetedUsers()) {
            addListingWarningIfNotValid(listing, tu.getTargetedUserName(), 
                    "Targeted User '" + tu.getTargetedUserName() + "'");
        }
        
        //check all criteria fields
        for(CertificationResult cert : listing.getCertificationResults()) {
            if(cert.isSuccess() != null && cert.isSuccess() == Boolean.TRUE) {
                addCriteriaWarningIfNotValid(listing, cert, cert.getApiDocumentation(), "API Documentation");
                if(cert.getAdditionalSoftware() != null) {
                    for(CertificationResultAdditionalSoftware addSoft : cert.getAdditionalSoftware()) {
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getName(), "Additional Software Name '" + addSoft.getName() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getVersion(), "Additional Software Version '" + addSoft.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                addSoft.getJustification(), "Additional Software Justification '" + addSoft.getJustification() + "'");
                    }
                }
                if(cert.getTestDataUsed() != null) {
                    for(CertificationResultTestData testData : cert.getTestDataUsed()) {
                        //not checking test data name because it has to match one of the existing names
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testData.getVersion(), "Test Data Version '" + testData.getVersion() + "'");
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testData.getAlteration(), "Test Data Alteration '" + testData.getAlteration() + "'");
                    }
                }
                
                //not checking test functionality name because it has to match one of the existing options

                if(cert.getTestProcedures() != null) {
                    for(CertificationResultTestProcedure testProc : cert.getTestProcedures()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, testProc.getTestProcedureVersion(), 
                                "Test Procedure Version '" + testProc.getTestProcedureVersion() + "'");
                    }
                }
                if(cert.getTestStandards() != null) {
                    for(CertificationResultTestStandard testStd : cert.getTestStandards()) {
                        addCriteriaWarningIfNotValid(listing, cert, 
                                testStd.getTestStandardName(), 
                                "Test Standard Name '" + testStd.getTestStandardName() + "'");
                    }
                }
                if(cert.getTestToolsUsed() != null) {
                    for(CertificationResultTestTool testTool : cert.getTestToolsUsed()) {
                        //not checking name because it has to match one of the existing options
                        addCriteriaWarningIfNotValid(listing, cert, testTool.getTestToolVersion(), 
                                "Test Tool Version '" + testTool.getTestToolVersion() + "'");
                    }
                }
            }
        }
        CertifiedProductSed sed = listing.getSed();
        if(sed != null) {
            if(sed.getTestTasks() != null) {
                for(TestTask task : sed.getTestTasks()) {
                    if(task != null) {
                        //not checking anything converted to a number
                        addListingWarningIfNotValid(listing, task.getDescription(), 
                                "Test Task Description '" + task.getDescription() + "'");
                        addListingWarningIfNotValid(listing, task.getTaskRatingScale() , 
                                "Test Task Rating Scale '" + task.getTaskRatingScale() + "'");
                    }
                    if(task.getTestParticipants() != null) {
                        for(TestParticipant participant : task.getTestParticipants()) {
                            if(participant != null) {
                                //not checking age range or education level because they have to map
                                //to existing values. also not checking anything converted to a number
                                addListingWarningIfNotValid(listing, participant.getAssistiveTechnologyNeeds() , 
                                        "Participant Assistive Technology Needs '" + participant.getAssistiveTechnologyNeeds() + "'");
                                addListingWarningIfNotValid(listing, participant.getGender(), 
                                        "Participant Gender '" + participant.getGender() + "'");
                                addListingWarningIfNotValid(listing, participant.getOccupation(), 
                                        "Participant Occupation '" + participant.getOccupation() + "'");
                            }
                        }
                    }
                }
            }
            if(sed.getUcdProcesses() != null) {
                for(UcdProcess ucd : sed.getUcdProcesses()) {
                    addListingWarningIfNotValid(listing, 
                            ucd.getName(), "UCD Process Name '" + ucd.getName() + "'");
                    addListingWarningIfNotValid(listing, 
                            ucd.getDetails(), "UCD Process Details '" + ucd.getDetails() + "'");
                }
            }
        }
    }
    
    private void addListingWarningIfNotValid(PendingCertifiedProductDTO listing, String input, String fieldName) {
        if(!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badCharacterFound"),
                                    LocaleContextHolder.getLocale()), fieldName));
        }
        
        if(ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("listing.newlineCharacterFound"),
                            LocaleContextHolder.getLocale()), fieldName));
        }
    }
    
    private void addCriteriaWarningIfNotValid(PendingCertifiedProductDTO listing, 
            PendingCertificationResultDTO criteria, String input, String fieldName) {
        if(!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badCharacterFound"),
                                    LocaleContextHolder.getLocale()), criteria.getNumber(), fieldName));
        }
        if(ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.newlineCharacterFound"),
                            LocaleContextHolder.getLocale()), criteria.getNumber(), fieldName));
        }
    }
    
    private void addListingWarningIfNotValid(CertifiedProductSearchDetails listing, String input, String fieldName) {
        if(!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badCharacterFound"),
                                    LocaleContextHolder.getLocale()), fieldName));
        }
        if(ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("listing.newlineCharacterFound"),
                            LocaleContextHolder.getLocale()), fieldName));
        }
    }
    
    private void addCriteriaWarningIfNotValid(CertifiedProductSearchDetails listing, 
            CertificationResult criteria, String input, String fieldName) {
        if(!ValidationUtils.isValidUtf8(input)) {
            listing.getWarningMessages().add(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badCharacterFound"),
                                    LocaleContextHolder.getLocale()), criteria.getNumber(), fieldName));
        }
        if(ValidationUtils.hasNewline(input)) {
            listing.getWarningMessages().add(String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.newlineCharacterFound"),
                            LocaleContextHolder.getLocale()), criteria.getNumber(), fieldName));
        }
    }
}
