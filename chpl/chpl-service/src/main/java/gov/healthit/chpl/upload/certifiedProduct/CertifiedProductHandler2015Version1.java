package gov.healthit.chpl.upload.certifiedProduct;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG1MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG2MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskParticipantEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductQmsStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCertificationCriteriaEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCqmCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingTestParticipantEntity;
import gov.healthit.chpl.entity.listing.pending.PendingTestTaskEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version1;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Component("certifiedProductHandler2015Version1")
public class CertifiedProductHandler2015Version1 extends CertifiedProductHandler {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2015Version1.class);
    private TemplateColumnIndexMap templateColumnIndexMap;
    private String[] criteriaNames = {
            "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)", "170.315 (a)(4)", "170.315 (a)(5)",
            "170.315 (a)(6)", "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(10)",
            "170.315 (a)(11)", "170.315 (a)(12)", "170.315 (a)(13)", "170.315 (a)(14)", "170.315 (a)(15)",
            "170.315 (b)(1)", "170.315 (b)(2)", "170.315 (b)(3)", "170.315 (b)(4)", "170.315 (b)(5)",
            "170.315 (b)(6)", "170.315 (b)(7)", "170.315 (b)(8)", "170.315 (b)(9)", "170.315 (c)(1)",
            "170.315 (c)(2)", "170.315 (c)(3)", "170.315 (c)(4)", "170.315 (d)(1)", "170.315 (d)(2)",
            "170.315 (d)(3)", "170.315 (d)(4)", "170.315 (d)(5)", "170.315 (d)(6)", "170.315 (d)(7)",
            "170.315 (d)(8)", "170.315 (d)(9)", "170.315 (d)(10)", "170.315 (d)(11)", "170.315 (e)(1)",
            "170.315 (e)(2)", "170.315 (e)(3)", "170.315 (f)(1)", "170.315 (f)(2)", "170.315 (f)(3)",
            "170.315 (f)(4)", "170.315 (f)(5)", "170.315 (f)(6)", "170.315 (f)(7)", "170.315 (g)(1)",
            "170.315 (g)(2)", "170.315 (g)(3)", "170.315 (g)(4)", "170.315 (g)(5)", "170.315 (g)(6)",
            "170.315 (g)(7)", "170.315 (g)(8)", "170.315 (g)(9)", "170.315 (h)(1)", "170.315 (h)(2)"
    };
    
    // we will ignore g1 and g2 macra measures for (g)(7) criteria for now
    // they shouldn't be there but it's hard for users to change the spreadsheet
    protected static final String G1_CRITERIA_TO_IGNORE = "170.315 (g)(7)";
    protected static final String G2_CRITERIA_TO_IGNORE = "170.315 (g)(7)";

    private List<PendingTestParticipantEntity> participants;
    private List<PendingTestTaskEntity> tasks;

    public CertifiedProductHandler2015Version1() {
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version1();
    }
    
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }
    
    public String[] getCriteriaNames() {
        return this.criteriaNames;
    }
    
    public PendingCertifiedProductEntity handle() throws InvalidArgumentsException {
        participants = new ArrayList<PendingTestParticipantEntity>();
        tasks = new ArrayList<PendingTestTaskEntity>();

        PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();
        pendingCertifiedProduct.setStatus(getDefaultStatusId());

        // get the first row of the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                parseCertifiedProductDetails(record, pendingCertifiedProduct);
            }
        }

        //
        //parse things that may have multi-row values
        //
        
        // get the targeted users for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTargetedUsers(record, pendingCertifiedProduct);
            }
        }

        // get the QMS's for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseQms(record, pendingCertifiedProduct);
            }
        }

        // get the accessibility standards for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseAccessibilityStandards(record, pendingCertifiedProduct);
            }
        }
        if (!pendingCertifiedProduct.getAccessibilityCertified()
                && pendingCertifiedProduct.getAccessibilityStandards().size() > 0) {
            pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId()
                    + " has 'false' in the Accessibility Certified column but accessibility standards were found.");
        } else if (pendingCertifiedProduct.getAccessibilityCertified()
                && pendingCertifiedProduct.getAccessibilityStandards().size() == 0) {
            pendingCertifiedProduct.getErrorMessages().add(pendingCertifiedProduct.getUniqueId()
                    + " has 'true' in the Accessibility Certified column but no accessibility standards were found.");
        }

        // parse CQMs
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseCqms(record, pendingCertifiedProduct);
            }
        }

        // test participant
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTestParticipants(record, pendingCertifiedProduct);
            }
        }

        // tasks
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(templateColumnIndexMap.getRecordStatusIndex()).trim();
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTestTasks(record, pendingCertifiedProduct);
            }
        }

        // parse criteria
        CSVRecord firstRow = null;
        for (int i = 0; i < getRecord().size() && firstRow == null; i++) {
            CSVRecord currRecord = getRecord().get(i);
            String statusStr = currRecord.get(templateColumnIndexMap.getRecordStatusIndex()).trim();
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                firstRow = currRecord;
            }
        }
        if (firstRow != null) {
            int criteriaBeginIndex = templateColumnIndexMap.getCriteriaStartIndex();
            for(int i = 0; i < getCriteriaNames().length; i++) {
                String criteriaName = getCriteriaNames()[i];
                int criteriaEndIndex = templateColumnIndexMap.getLastIndexForCriteria(getHeading(), criteriaBeginIndex);
                pendingCertifiedProduct.getCertificationCriterion().add(parseCriteria(pendingCertifiedProduct,
                        criteriaName, firstRow, criteriaBeginIndex, criteriaEndIndex));
                criteriaBeginIndex = criteriaEndIndex + 1;
            }
        }

        return pendingCertifiedProduct;
    }

    protected void parseCertifiedProductDetails(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        parseUniqueId(pendingCertifiedProduct, record);
        parseRecordStatus(pendingCertifiedProduct, record);
        parseDeveloperProductVersion(pendingCertifiedProduct, record);
        parseDeveloperAddress(pendingCertifiedProduct, record);
        parseEdition("2015", pendingCertifiedProduct, record);
        parseAcbCertificationId(pendingCertifiedProduct, record);
        parseAcb(pendingCertifiedProduct, record);
        parseAtl(pendingCertifiedProduct, record);
        parseCertificationDate(pendingCertifiedProduct, record);        
        parseSed(pendingCertifiedProduct, record);
        parseHasIcs(pendingCertifiedProduct, record);
        parseTransparencyAttestation(pendingCertifiedProduct, record);
        
        // accessibility certified
        String isAccessibilityCertified = record.get(templateColumnIndexMap.getAccessibilityCertifiedIndex()).trim();
        pendingCertifiedProduct.setAccessibilityCertified(asBoolean(isAccessibilityCertified));
    }
    
    @Override
    protected void parseSed(PendingCertifiedProductEntity pendingCertifiedProduct, CSVRecord record) {
        int sedIndex = getColumnIndexMap().getSedStartIndex();
        pendingCertifiedProduct.setSedReportFileLocation(record.get(sedIndex++).trim());
        pendingCertifiedProduct.setSedIntendedUserDescription(record.get(sedIndex++).trim());
        String sedTestingEnd = record.get(sedIndex++).trim();
        if (!StringUtils.isEmpty(sedTestingEnd)) {
            try {
                Date sedTestingEndDate = dateFormatter.parse(sedTestingEnd);
                pendingCertifiedProduct.setSedTestingEnd(sedTestingEndDate);
            } catch (final ParseException ex) {
                LOGGER.error("Could not parse " + sedTestingEnd, ex);
                pendingCertifiedProduct.getErrorMessages().add("Product " + pendingCertifiedProduct.getUniqueId()
                        + " has an invalid sed testing end date '" + sedTestingEnd + "'.");
            }
        }    
    }
    
    protected int parseQms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = templateColumnIndexMap.getQmsStartIndex();
        if (!StringUtils.isEmpty(record.get(colIndex))) {
            String qmsStandardName = record.get(colIndex++).trim();
            QmsStandardDTO qmsStandard = qmsDao.getByName(qmsStandardName);
            String applicableCriteria = record.get(colIndex++).trim();
            String qmsMods = record.get(colIndex).trim();

            PendingCertifiedProductQmsStandardEntity qmsEntity = new PendingCertifiedProductQmsStandardEntity();
            qmsEntity.setMappedProduct(pendingCertifiedProduct);
            qmsEntity.setModification(qmsMods);
            qmsEntity.setApplicableCriteria(applicableCriteria);
            qmsEntity.setName(qmsStandardName);
            if (qmsStandard != null) {
                qmsEntity.setQmsStandardId(qmsStandard.getId());
            }
            pendingCertifiedProduct.getQmsStandards().add(qmsEntity);
        }
        return (templateColumnIndexMap.getQmsEndIndex() - templateColumnIndexMap.getQmsStartIndex()) + 1;
    }

    protected int parseTargetedUsers(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = templateColumnIndexMap.getTargetedUserStartIndex();
        if (!StringUtils.isEmpty(record.get(colIndex))) {
            String targetedUserName = record.get(colIndex).trim();
            TargetedUserDTO targetedUser = tuDao.getByName(targetedUserName);

            PendingCertifiedProductTargetedUserEntity tuEntity = new PendingCertifiedProductTargetedUserEntity();
            tuEntity.setMappedProduct(pendingCertifiedProduct);
            tuEntity.setName(targetedUserName);
            if (targetedUser != null) {
                tuEntity.setTargetedUserId(targetedUser.getId());
            }
            pendingCertifiedProduct.getTargetedUsers().add(tuEntity);
        }
        return (templateColumnIndexMap.getTargetedUserEndIndex() - templateColumnIndexMap.getTargetedUserStartIndex()) + 1;
    }

    protected int parseAccessibilityStandards(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = templateColumnIndexMap.getAccessibilityStandardIndex();
        if (!StringUtils.isEmpty(record.get(colIndex))) {
            String accessibilityStandardName = record.get(colIndex).trim();
            AccessibilityStandardDTO std = stdDao.getByName(accessibilityStandardName);

            PendingCertifiedProductAccessibilityStandardEntity stdEntity = new PendingCertifiedProductAccessibilityStandardEntity();
            stdEntity.setMappedProduct(pendingCertifiedProduct);
            stdEntity.setName(accessibilityStandardName);
            if (std != null) {
                stdEntity.setAccessibilityStandardId(std.getId());
            }
            pendingCertifiedProduct.getAccessibilityStandards().add(stdEntity);
        }
        return 1;
    }

    protected int parseTestParticipants(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProductEntity)
            throws InvalidArgumentsException {
        int colIndex = templateColumnIndexMap.getTestParticipantStartIndex();
        if (!StringUtils.isEmpty(record.get(colIndex))) {
            PendingTestParticipantEntity participant = new PendingTestParticipantEntity();
            participant.setUniqueId(record.get(colIndex++).trim());
            participant.setGender(record.get(colIndex++).trim());
            String ageStr = record.get(colIndex++).trim();
            if (StringUtils.isEmpty(ageStr)) {
                LOGGER.error("Age range is empty.");
            } else {
                participant.setUserEnteredAge(ageStr);
                AgeRangeDTO ageDto = ageDao.getByName(ageStr);
                if (ageDto != null) {
                    participant.setAgeRangeId(ageDto.getId());
                } else {
                    LOGGER.error("Age range '" + ageStr + "' does not match any of the allowed values.");
                }
            }

            String educationLevel = record.get(colIndex++).trim();
            if (StringUtils.isEmpty(educationLevel)) {
                LOGGER.error("Education level is empty.");
            } else {
                participant.setUserEnteredEducation(educationLevel);
                EducationTypeDTO educationDto = educationDao.getByName(educationLevel);
                if (educationDto != null) {
                    participant.setEducationTypeId(educationDto.getId());
                } else {
                    LOGGER.error("Education level '" + educationLevel + "' does not match any of the allowed options.");
                }
            }
            participant.setOccupation(record.get(colIndex++).trim());
            String profExperienceStr = record.get(colIndex++).trim();
            try {
                Integer profExperience = Math.round(new Float(profExperienceStr));
                participant.setProfessionalExperienceMonths(profExperience);
            } catch (Exception ex) {
                LOGGER.error("Could not parse " + profExperienceStr + " into an integer.");
            }

            String computerExperienceStr = record.get(colIndex++).trim();
            try {
                Integer computerExperience = Math.round(new Float(computerExperienceStr));
                participant.setComputerExperienceMonths(computerExperience);
            } catch (Exception ex) {
                LOGGER.error("Could not parse " + computerExperienceStr + " into an integer.");
            }

            String productExperienceStr = record.get(colIndex++).trim();
            try {
                Integer productExperience = Math.round(new Float(productExperienceStr));
                participant.setProductExperienceMonths(productExperience);
            } catch (Exception ex) {
                LOGGER.error("Could not parse " + productExperienceStr + " into an integer.");
            }

            participant.setAssistiveTechnologyNeeds(record.get(colIndex).trim());
            this.participants.add(participant);
        }
        return (templateColumnIndexMap.getTestParticipantEndIndex() - templateColumnIndexMap.getTestParticipantStartIndex()) + 1;
    }

    protected int parseTestTasks(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProductEntity) {
        int colIndex = templateColumnIndexMap.getTestTaskStartIndex();
        if (StringUtils.isEmpty(record.get(colIndex))) {
            return 0;
        }

        PendingTestTaskEntity task = new PendingTestTaskEntity();
        task.setUniqueId(record.get(colIndex++).trim());
        task.setDescription(record.get(colIndex++).trim());
        String successAvgStr = record.get(colIndex++).trim();
        try {
            Float successAvg = new Float(successAvgStr);
            task.setTaskSuccessAverage(successAvg);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + successAvgStr + " to a Float.");
        }
        String successStddevStr = record.get(colIndex++).trim();
        try {
            Float successStddev = new Float(successStddevStr);
            task.setTaskSuccessStddev(successStddev);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + successStddevStr + " to a Float.");
        }
        String taskPathDeviationObsStr = record.get(colIndex++).trim();
        try {
            Integer taskPathDeviationObs = Math.round(new Float(taskPathDeviationObsStr));
            task.setTaskPathDeviationObserved(taskPathDeviationObs);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskPathDeviationObsStr + " to a Integer.");
        }
        String taskPathDeviationOptStr = record.get(colIndex++).trim();
        try {
            Integer taskPathDeviationOpt = Math.round(new Float(taskPathDeviationOptStr));
            task.setTaskPathDeviationOptimal(taskPathDeviationOpt);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskPathDeviationOptStr + " to a Integer.");
        }
        String taskTimeAvgStr = record.get(colIndex++).trim();
        try {
            Integer taskTimeAvg = Math.round(new Float(taskTimeAvgStr));
            task.setTaskTimeAvg(new Long(taskTimeAvg));
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskTimeAvgStr + " to a Integer.");
        }
        String taskTimeStddevStr = record.get(colIndex++).trim();
        try {
            Integer taskTimeStddev = Math.round(new Float(taskTimeStddevStr));
            task.setTaskTimeStddev(taskTimeStddev);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskTimeStddevStr + " to a Integer.");
        }
        String taskTimeDeviationAvgStr = record.get(colIndex++).trim();
        try {
            Integer taskTimeDeviationAvg = Math.round(new Float(taskTimeDeviationAvgStr));
            task.setTaskTimeDeviationObservedAvg(taskTimeDeviationAvg);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskTimeDeviationAvgStr + " to a Integer.");
        }
        String taskTimeDeviationOptimalAvgStr = record.get(colIndex++).trim();
        try {
            Integer taskTimeDeviationOptimalAvg = Math.round(new Float(taskTimeDeviationOptimalAvgStr));
            task.setTaskTimeDeviationOptimalAvg(taskTimeDeviationOptimalAvg);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskTimeDeviationOptimalAvgStr + " to a Integer.");
        }
        String taskErrorsAvgStr = record.get(colIndex++).trim();
        try {
            Float taskErrorsAvg = new Float(taskErrorsAvgStr);
            task.setTaskErrors(taskErrorsAvg);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskErrorsAvgStr + " to a Float.");
        }
        String taskErrorsStddevStr = record.get(colIndex++).trim();
        try {
            Float taskErrorsStddev = new Float(taskErrorsStddevStr);
            task.setTaskErrorsStddev(taskErrorsStddev);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskErrorsStddevStr + " to a Float.");
        }
        task.setTaskRatingScale(record.get(colIndex++).trim());
        String taskRatingStr = record.get(colIndex++).trim();
        try {
            Float taskRating = new Float(taskRatingStr);
            task.setTaskRating(taskRating);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskRatingStr + " to a Float.");
        }

        String taskRatingStddevStr = record.get(colIndex++).trim();
        try {
            Float taskRatingStddev = new Float(taskRatingStddevStr);
            task.setTaskRatingStddev(taskRatingStddev);
        } catch (Exception ex) {
            LOGGER.error("Cannot convert " + taskRatingStddevStr + " to a Float.");
        }
        this.tasks.add(task);
        return (templateColumnIndexMap.getTestTaskEndIndex() - templateColumnIndexMap.getTestTaskStartIndex()) + 1;
    }

    protected int parseCqms(CSVRecord record, PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = templateColumnIndexMap.getCqmStartIndex();
        
        String cqmName = record.get(colIndex++).trim();
        String cqmVersions = record.get(colIndex++).trim();
        String cqmCriteria = record.get(colIndex).trim();

        List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion(pendingCertifiedProduct, cqmName, cqmVersions,
                cqmCriteria);
        for (PendingCqmCriterionEntity entity : criterion) {
            if (entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria() == Boolean.TRUE) {
                pendingCertifiedProduct.getCqmCriterion().add(entity);
            }
        }
        return (templateColumnIndexMap.getCqmEndIndex() - templateColumnIndexMap.getCqmStartIndex()) + 1;
    }

    protected PendingCertificationResultEntity parseCriteria(PendingCertifiedProductEntity pendingCertifiedProduct,
            String criteriaNumber, CSVRecord firstRow, int beginIndex, int endIndex) {
        int currIndex = beginIndex;
        PendingCertificationResultEntity cert = null;
        try {
            cert = getCertificationResult(criteriaNumber, firstRow.get(currIndex++).toString());

            while (currIndex <= endIndex) {
                String colTitle = getHeading().get(currIndex).trim();
                if (!StringUtils.isEmpty(colTitle)) {
                    colTitle = colTitle.trim().toUpperCase();
                    switch (colTitle) {
                    case "GAP":
                        cert.setGap(asBoolean(firstRow.get(currIndex++).trim()));
                        break;
                    case "PRIVACY AND SECURITY FRAMEWORK":
                        cert.setPrivacySecurityFramework(
                                CertificationResult.formatPrivacyAndSecurityFramework(firstRow.get(currIndex++)));
                        break;
                    case "API DOCUMENTATION LINK":
                        cert.setApiDocumentation(firstRow.get(currIndex++).trim());
                        break;
                    case "STANDARD TESTED AGAINST":
                        parseTestStandards(pendingCertifiedProduct, cert, currIndex);
                        currIndex++;
                        break;
                    case "FUNCTIONALITY TESTED":
                        parseTestFunctionality(pendingCertifiedProduct, cert, currIndex);
                        currIndex++;
                        break;
                    case "MEASURE SUCCESSFULLY TESTED FOR G1":
                        parseG1Measures(cert, currIndex);
                        currIndex++;
                        break;
                    case "MEASURE SUCCESSFULLY TESTED FOR G2":
                        parseG2Measures(cert, currIndex);
                        currIndex++;
                        break;
                    case "ADDITIONAL SOFTWARE":
                        Boolean hasAdditionalSoftware = asBoolean(firstRow.get(currIndex).trim());
                        cert.setHasAdditionalSoftware(hasAdditionalSoftware);
                        parseAdditionalSoftware(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 6;
                        break;
                    case "TEST TOOL NAME":
                        parseTestTools(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 2;
                    case "TEST PROCEDURE VERSION":
                        parseTestProcedures(cert, currIndex);
                        currIndex++;
                        break;
                    case "TEST DATA VERSION":
                        parseTestData(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 3;
                        break;
                    case "UCD PROCESS SELECTED":
                        PendingCertificationResultUcdProcessEntity ucd = new PendingCertificationResultUcdProcessEntity();
                        String ucdName = firstRow.get(currIndex++).trim();
                        String ucdDetails = firstRow.get(currIndex++).trim();

                        if (!StringUtils.isEmpty(ucdName)) {
                            ucd.setUcdProcessName(ucdName);
                            ucd.setUcdProcessDetails(ucdDetails);
                            UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
                            if (dto != null) {
                                ucd.setUcdProcessId(dto.getId());
                            }
                            cert.getUcdProcesses().add(ucd);
                        }
                        break;
                    case "TASK IDENTIFIER":
                        parseTasksAndParticipants(pendingCertifiedProduct, cert, currIndex);
                        currIndex += 2;
                        break;
                    default:
                        pendingCertifiedProduct.getErrorMessages()
                                .add("Invalid column title " + colTitle + " at index " + currIndex);
                        LOGGER.error("Could not handle column " + colTitle + " at index " + currIndex + ".");
                        currIndex++;
                    }
                }
            }
        } catch (final InvalidArgumentsException ex) {
            LOGGER.error(ex.getMessage());
        }
        return cert;
    }

    protected void parseTestStandards(PendingCertifiedProductEntity listing, PendingCertificationResultEntity cert,
            int tsColumn) {
        for (CSVRecord row : getRecord()) {
            String tsValue = row.get(tsColumn).trim();
            if (!StringUtils.isEmpty(tsValue)) {
                PendingCertificationResultTestStandardEntity tsEntity = new PendingCertificationResultTestStandardEntity();
                tsEntity.setTestStandardName(tsValue);
                TestStandardDTO ts = testStandardDao.getByNumberAndEdition(tsValue,
                        listing.getCertificationEditionId());
                if (ts != null) {
                    tsEntity.setTestStandardId(ts.getId());
                }
                cert.getTestStandards().add(tsEntity);
            }
        }
    }

    protected void parseTestFunctionality(PendingCertifiedProductEntity listing, PendingCertificationResultEntity cert,
            int tfColumn) {
        for (CSVRecord row : getRecord()) {
            String tfValue = row.get(tfColumn).trim();
            if (!StringUtils.isEmpty(tfValue)) {
                PendingCertificationResultTestFunctionalityEntity tfEntity = new PendingCertificationResultTestFunctionalityEntity();
                tfEntity.setTestFunctionalityNumber(tfValue);
                TestFunctionalityDTO tf = testFunctionalityDao.getByNumberAndEdition(tfValue,
                        listing.getCertificationEditionId());
                if (tf != null) {
                    tfEntity.setTestFunctionalityId(tf.getId());
                }
                cert.getTestFunctionality().add(tfEntity);
            }
        }
    }

    protected void parseAdditionalSoftware(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert,
            int asColumnBegin) {
        int cpSourceColumn = asColumnBegin + 1;
        int nonCpSourceColumn = asColumnBegin + 3;

        for (CSVRecord row : getRecord()) {
            String cpSourceValue = row.get(cpSourceColumn).toString().trim();
            if (!StringUtils.isEmpty(cpSourceValue)) {
                PendingCertificationResultAdditionalSoftwareEntity asEntity = new PendingCertificationResultAdditionalSoftwareEntity();
                asEntity.setChplId(cpSourceValue);
                if (cpSourceValue.startsWith("CHP-")) {
                    CertifiedProductDTO cp = certifiedProductDao.getByChplNumber(cpSourceValue);
                    if (cp != null) {
                        asEntity.setCertifiedProductId(cp.getId());
                    }
                } else {
                    try {
                        CertifiedProductDetailsDTO cpd = certifiedProductDao.getByChplUniqueId(cpSourceValue);
                        if (cpd != null) {
                            asEntity.setCertifiedProductId(cpd.getId());
                        }
                    } catch (final EntityRetrievalException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
                asEntity.setGrouping(row.get(cpSourceColumn + 1).toString().trim());
                cert.getAdditionalSoftware().add(asEntity);
            }
            String nonCpSourceValue = row.get(nonCpSourceColumn).toString().trim();
            if (!StringUtils.isEmpty(nonCpSourceValue)) {
                PendingCertificationResultAdditionalSoftwareEntity asEntity = new PendingCertificationResultAdditionalSoftwareEntity();
                asEntity.setSoftwareName(nonCpSourceValue);
                asEntity.setSoftwareVersion(row.get(nonCpSourceColumn + 1).toString().trim());
                asEntity.setGrouping(row.get(nonCpSourceColumn + 2).toString().trim());
                cert.getAdditionalSoftware().add(asEntity);
            }
        }

        if (cert.getHasAdditionalSoftware() != null && cert.getHasAdditionalSoftware().booleanValue() == true
                && cert.getAdditionalSoftware().size() == 0) {
            product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                    + product.getUniqueId() + " indicates additional software should be present but none was found.");
        } else if ((cert.getHasAdditionalSoftware() == null || cert.getHasAdditionalSoftware().booleanValue() == false)
                && cert.getAdditionalSoftware().size() > 0) {
            product.getErrorMessages()
                    .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                            + product.getUniqueId()
                            + " indicates additional software should not be present but some was found.");
        }
    }

    protected void parseTestProcedures(PendingCertificationResultEntity cert, int tpColumn) {
        for (CSVRecord row : getRecord()) {
            String tpValue = row.get(tpColumn).trim();
            if (!StringUtils.isEmpty(tpValue)) {
                PendingCertificationResultTestProcedureEntity tpEntity = new PendingCertificationResultTestProcedureEntity();
                tpEntity.setTestProcedureVersion(tpValue);
                // don't look up by name because we don't want these to be
                // shared
                // among certifications. they are user-entered, could be
                // anything, and if
                // they are shared then updating in one place could affect other
                // places
                // when that is not the intended behavior
                cert.getTestProcedures().add(tpEntity);
            }
        }
    }

    protected void parseTestData(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert,
            int tdColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String tdVersionValue = row.get(tdColumnBegin).trim();
            if (!StringUtils.isEmpty(tdVersionValue)) {
                PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
                tdEntity.setVersion(tdVersionValue);
                Boolean hasAlteration = asBoolean(row.get(tdColumnBegin + 1).trim());
                tdEntity.setHasAlteration(hasAlteration);
                String alterationStr = row.get(tdColumnBegin + 2).trim();
                if (tdEntity.isHasAlteration() && StringUtils.isEmpty(alterationStr)) {
                    product.getErrorMessages()
                            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                    + product.getUniqueId()
                                    + " indicates test data was altered however no test data alteration was found.");
                } else if (!tdEntity.isHasAlteration() && !StringUtils.isEmpty(alterationStr)) {
                    product.getErrorMessages()
                            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                    + product.getUniqueId()
                                    + " indicates test data was not altered however a test data alteration was found.");
                }
                tdEntity.setAlteration(row.get(tdColumnBegin + 2).trim());
                cert.getTestData().add(tdEntity);
            }
        }
    }

    protected void parseTestTools(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert,
            int toolColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String testToolName = row.get(toolColumnBegin).trim();
            String testToolVersion = row.get(toolColumnBegin + 1).trim();
            if (!StringUtils.isEmpty(testToolName)) {
                PendingCertificationResultTestToolEntity ttEntity = new PendingCertificationResultTestToolEntity();
                ttEntity.setTestToolName(testToolName);
                ttEntity.setTestToolVersion(testToolVersion);
                TestToolDTO testTool = testToolDao.getByName(testToolName);
                if (testTool != null) {
                    ttEntity.setTestToolId(testTool.getId());
                }
                cert.getTestTools().add(ttEntity);
            }
        }
    }

    protected void parseG1Measures(PendingCertificationResultEntity cert, int measureCol) {
        // ignore measures for G7
        if (cert.getMappedCriterion().getNumber().equals(G1_CRITERIA_TO_IGNORE)) {
            return;
        }

        for (CSVRecord row : getRecord()) {
            String measureVal = row.get(measureCol).trim();
            if (!StringUtils.isEmpty(measureVal) && !measureVal.equalsIgnoreCase(Boolean.FALSE.toString())
                    && !measureVal.equals("0")) {
                PendingCertificationResultG1MacraMeasureEntity mmEntity = new PendingCertificationResultG1MacraMeasureEntity();
                mmEntity.setEnteredValue(measureVal);
                MacraMeasureDTO mmDto = macraDao.getByCriteriaNumberAndValue(cert.getMappedCriterion().getNumber(),
                        measureVal);
                if (mmDto != null) {
                    mmEntity.setMacraId(mmDto.getId());
                }
                cert.getG1MacraMeasures().add(mmEntity);
            }
        }
    }

    protected void parseG2Measures(PendingCertificationResultEntity cert, int measureCol) {
        // ignore measures for G7
        if (cert.getMappedCriterion().getNumber().equals(G2_CRITERIA_TO_IGNORE)) {
            return;
        }

        for (CSVRecord row : getRecord()) {
            String measureVal = row.get(measureCol).trim();
            if (!StringUtils.isEmpty(measureVal) && !measureVal.equalsIgnoreCase(Boolean.FALSE.toString())
                    && !measureVal.equals("0")) {
                PendingCertificationResultG2MacraMeasureEntity mmEntity = new PendingCertificationResultG2MacraMeasureEntity();
                mmEntity.setEnteredValue(measureVal.trim());
                MacraMeasureDTO mmDto = macraDao.getByCriteriaNumberAndValue(cert.getMappedCriterion().getNumber(),
                        measureVal);
                if (mmDto != null) {
                    mmEntity.setMacraId(mmDto.getId());
                }
                cert.getG2MacraMeasures().add(mmEntity);
            }
        }
    }

    protected void parseTasksAndParticipants(PendingCertifiedProductEntity product, PendingCertificationResultEntity cert,
            int taskColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String taskUniqueId = row.get(taskColumnBegin).trim();
            if (!StringUtils.isEmpty(taskUniqueId)) {
                PendingTestTaskEntity taskEntity = null;
                for (PendingTestTaskEntity task : this.tasks) {
                    if (task.getUniqueId().equals(taskUniqueId)) {
                        taskEntity = task;
                    }
                }
                if (taskEntity == null) {
                    product.getErrorMessages()
                            .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                    + product.getUniqueId() + " has no task with unique id " + taskUniqueId
                                    + " defined in the file.");
                } else {
                    PendingCertificationResultTestTaskEntity certTask = new PendingCertificationResultTestTaskEntity();
                    certTask.setTestTask(taskEntity);

                    String participantUniqueIdStr = row.get(taskColumnBegin + 1).trim();
                    String[] participantUniqueIds = participantUniqueIdStr.split(";");
                    for (int i = 0; i < participantUniqueIds.length; i++) {
                        PendingTestParticipantEntity participantEntity = null;
                        for (PendingTestParticipantEntity participant : this.participants) {
                            if (participant.getUniqueId().equals(participantUniqueIds[i].trim())) {
                                participantEntity = participant;
                            }
                        }
                        if (participantEntity == null) {
                            product.getErrorMessages()
                                    .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                                            + product.getUniqueId() + " has no participant with unique id '"
                                            + participantUniqueIds[i] + "'  defined in the file.");
                        } else {
                            PendingCertificationResultTestTaskParticipantEntity ttPartEntity = new PendingCertificationResultTestTaskParticipantEntity();
                            ttPartEntity.setCertTestTask(certTask);
                            ttPartEntity.setTestParticipant(participantEntity);
                            certTask.getTestParticipants().add(ttPartEntity);
                        }
                    }
                    cert.getTestTasks().add(certTask);
                }
            }
        }
    }

    public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms) {
        List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
        for (CQMCriterion criterion : allCqms) {
            if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
                criteria.add(criterion);
            }
        }
        return criteria;
    }

    /**
     * look up a CQM CMS criteria by number and version. throw an error if we
     * can't find it
     * 
     * @param criterionNum
     * @param column
     * @return
     * @throws InvalidArgumentsException
     */
    protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(PendingCertifiedProductEntity product,
            String criterionNum, String version, String mappedCriteria) {
        if (!StringUtils.isEmpty(version)) {
            version = version.trim();
        }

        List<PendingCqmCriterionEntity> result = new ArrayList<PendingCqmCriterionEntity>();

        if (!StringUtils.isEmpty(version) && !"0".equals(version)) {
            // split on ;
            String[] versionList = version.split(";");
            if (versionList.length == 1) {
                // also try splitting on ,
                versionList = version.split(",");
            }

            for (int i = 0; i < versionList.length; i++) {
                String currVersion = versionList[i].trim();
                if (!criterionNum.startsWith("CMS")) {
                    criterionNum = "CMS" + criterionNum;
                }
                CQMCriterionEntity cqmEntity = cqmDao.getCMSEntityByNumberAndVersion(criterionNum, currVersion);
                if (cqmEntity == null) {
                    product.getErrorMessages().add("Could not find a CQM CMS criterion matching " + criterionNum
                            + " and version " + currVersion + " for product " + product.getUniqueId());
                }

                PendingCqmCriterionEntity currResult = new PendingCqmCriterionEntity();
                currResult.setMappedCriterion(cqmEntity);
                currResult.setMeetsCriteria(true);

                // check on mapped criteria
                if (!StringUtils.isEmpty(mappedCriteria) && !"0".equals(mappedCriteria)) {
                    // split on ;
                    String[] criteriaList = mappedCriteria.split(";");
                    if (criteriaList.length == 1) {
                        // also try splitting on ,
                        criteriaList = mappedCriteria.split(",");
                    }

                    for (int j = 0; j < criteriaList.length; j++) {
                        String currCriteria = criteriaList[j].trim();
                        CertificationCriterionDTO cert = null;
                        if (currCriteria.startsWith("170.315")) {
                            cert = certDao.getByName(currCriteria);
                        } else if (currCriteria.equals("c1")) {
                            cert = certDao.getByName("170.315 (c)(1)");
                        } else if (currCriteria.equals("c2")) {
                            cert = certDao.getByName("170.315 (c)(2)");
                        } else if (currCriteria.equals("c3")) {
                            cert = certDao.getByName("170.315 (c)(3)");
                        } else if (currCriteria.equals("c4")) {
                            cert = certDao.getByName("170.315 (c)(4)");
                        }
                        if (cert != null) {
                            PendingCqmCertificationCriteriaEntity certEntity = new PendingCqmCertificationCriteriaEntity();
                            certEntity.setCertificationId(cert.getId());
                            CertificationCriterionEntity criteriaEntity = new CertificationCriterionEntity();
                            criteriaEntity.setId(cert.getId());
                            criteriaEntity.setNumber(cert.getNumber());
                            criteriaEntity.setTitle(cert.getTitle());
                            certEntity.setCertificationCriteria(criteriaEntity);
                            currResult.getCertifications().add(certEntity);
                        } else {
                            product.getErrorMessages().add("Could not find a certification criteria matching "
                                    + currCriteria + " for product " + product.getUniqueId());
                        }
                    }
                }
                result.add(currResult);
            }
        }

        return result;
    }
}
