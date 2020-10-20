package gov.healthit.chpl.upload.certifiedProduct;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.MipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.dto.AgeRangeDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.EducationTypeDTO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.dto.TestStandardDTO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
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
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.listing.mipsMeasure.PendingListingMipsMeasureCriterionMapEntity;
import gov.healthit.chpl.listing.mipsMeasure.PendingListingMipsMeasureEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version1;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certifiedProductHandler2015Version1")
public class CertifiedProductHandler2015Version1 extends CertifiedProductHandler {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductHandler2015Version1.class);
    private ErrorMessageUtil msgUtil;
    private TemplateColumnIndexMap templateColumnIndexMap;
    private static final String G1_MEASUREMENT_TYPE_NAME = "G1";
    private static final String G2_MEASUREMENT_TYPE_NAME = "G2";

    // we will ignore g1 and g2 macra measures for (g)(7) criteria for now
    // they shouldn't be there but it's hard for users to change the spreadsheet
    protected static final String G1_CRITERIA_TO_IGNORE = "170.315 (g)(7)";
    protected static final String G2_CRITERIA_TO_IGNORE = "170.315 (g)(7)";

    private List<PendingTestParticipantEntity> participants;
    private List<PendingTestTaskEntity> tasks;

    @Autowired
    public CertifiedProductHandler2015Version1(ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version1();
    }

    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }

    public PendingCertifiedProductEntity handle() throws InvalidArgumentsException {
        participants = new ArrayList<PendingTestParticipantEntity>();
        tasks = new ArrayList<PendingTestTaskEntity>();

        PendingCertifiedProductEntity pendingCertifiedProduct = new PendingCertifiedProductEntity();

        // get the first row of the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                parseCertifiedProductDetails(record, pendingCertifiedProduct);
            }
        }

        //
        // parse things that may have multi-row values
        //

        // get ATL(s) for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseAtl(pendingCertifiedProduct, record);
            }
        }

        // get the targeted users for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTargetedUsers(record, pendingCertifiedProduct);
            }
        }

        // get the QMS's for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseQms(record, pendingCertifiedProduct);
            }
        }

        // get the accessibility standards for the certified product
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
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
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseCqms(record, pendingCertifiedProduct);
            }
        }

        // test participant
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex());
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTestParticipants(record, pendingCertifiedProduct);
            }
        }

        // tasks
        for (CSVRecord record : getRecord()) {
            String statusStr = record.get(getColumnIndexMap().getRecordStatusIndex()).trim();
            if (!StringUtils.isEmpty(statusStr) && (FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)
                    || SUBSEQUENT_ROW_INDICATOR.equalsIgnoreCase(statusStr))) {
                parseTestTasks(record, pendingCertifiedProduct);
            }
        }

        // parse criteria
        CSVRecord firstRow = null;
        for (int i = 0; i < getRecord().size() && firstRow == null; i++) {
            CSVRecord currRecord = getRecord().get(i);
            String statusStr = currRecord.get(getColumnIndexMap().getRecordStatusIndex()).trim();
            if (!StringUtils.isEmpty(statusStr) && FIRST_ROW_INDICATOR.equalsIgnoreCase(statusStr)) {
                firstRow = currRecord;
            }
        }
        if (firstRow != null) {
            // get all 2015 criteria and track whether they were added to the
            // listing via the upload parsing
            // add them after all the parsing is done if they were not present
            // in the upload
            List<CertificationCriterionDTO> all2015Criteria = certDao
                    .findByCertificationEditionYear(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
            Map<Long, Boolean> allCriteriaMap = new LinkedHashMap<Long, Boolean>();
            for (CertificationCriterionDTO criterion : all2015Criteria) {
                allCriteriaMap.put(criterion.getId(), Boolean.FALSE);
            }

            int criteriaBeginIndex = getColumnIndexMap().getCriteriaStartIndex();
            int criteriaEndIndex = getColumnIndexMap().getLastIndexForCriteria(getHeading(), criteriaBeginIndex);
            while (criteriaEndIndex > 0 && criteriaEndIndex <= getColumnIndexMap().getCriteriaEndIndex()) {
                PendingCertificationResultEntity certEntity = parseCriteria(pendingCertifiedProduct,
                        firstRow, criteriaBeginIndex, criteriaEndIndex);
                if (certEntity != null) {
                    pendingCertifiedProduct.getCertificationCriterion().add(certEntity);
                    if (certEntity.getMappedCriterion() != null) {
                        allCriteriaMap.put(certEntity.getMappedCriterion().getId(), Boolean.TRUE);
                    }
                }

                criteriaBeginIndex = Math.min(getColumnIndexMap().getCriteriaEndIndex(), criteriaEndIndex + 1);
                criteriaEndIndex = getColumnIndexMap().getLastIndexForCriteria(getHeading(), criteriaBeginIndex);
            }

            // add certification results for any criteria that are part of the
            // edition but that weren't in the upload file
            for (Long criterionId : allCriteriaMap.keySet()) {
                if (allCriteriaMap.get(criterionId).equals(Boolean.FALSE)) {
                    pendingCertifiedProduct.getCertificationCriterion()
                            .add(getCertificationResult(criterionId, ""));
                }
            }
        }

        return pendingCertifiedProduct;
    }

    protected void parseCertifiedProductDetails(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProduct) {
        parseUniqueId(pendingCertifiedProduct, record);
        parseRecordStatus(pendingCertifiedProduct, record);
        parseDeveloperProductVersion(pendingCertifiedProduct, record);
        parseDeveloperDetails(pendingCertifiedProduct, record);
        parseEdition("2015", pendingCertifiedProduct, record);
        parseAcbCertificationId(pendingCertifiedProduct, record);
        parseAcb(pendingCertifiedProduct, record);
        parseCertificationDate(pendingCertifiedProduct, record);
        parseSed(pendingCertifiedProduct, record);
        parseHasIcs(pendingCertifiedProduct, record);
        parseTransparencyAttestation(pendingCertifiedProduct, record);

        // accessibility certified
        String isAccessibilityCertified = record.get(getColumnIndexMap().getAccessibilityCertifiedIndex()).trim();
        pendingCertifiedProduct.setAccessibilityCertified(asBoolean(isAccessibilityCertified));
    }

    protected void parseSed(final PendingCertifiedProductEntity pendingCertifiedProduct, final CSVRecord record) {
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

    protected int parseQms(final CSVRecord record, final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getQmsStartIndex();
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
        return (getColumnIndexMap().getQmsEndIndex() - getColumnIndexMap().getQmsStartIndex()) + 1;
    }

    protected int parseTargetedUsers(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getTargetedUserStartIndex();
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
        return (getColumnIndexMap().getTargetedUserEndIndex() - getColumnIndexMap().getTargetedUserStartIndex()) + 1;
    }

    protected int parseAccessibilityStandards(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getAccessibilityStandardIndex();
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

    protected int parseTestParticipants(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProductEntity) throws InvalidArgumentsException {
        int colIndex = getColumnIndexMap().getTestParticipantStartIndex();
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
            participant.setProfessionalExperienceMonths(record.get(colIndex++).trim());
            participant.setComputerExperienceMonths(record.get(colIndex++).trim());
            participant.setProductExperienceMonths(record.get(colIndex++).trim());
            participant.setAssistiveTechnologyNeeds(record.get(colIndex).trim());
            this.participants.add(participant);
        }
        return (getColumnIndexMap().getTestParticipantEndIndex() - getColumnIndexMap().getTestParticipantStartIndex())
                + 1;
    }

    protected int parseTestTasks(final CSVRecord record,
            final PendingCertifiedProductEntity pendingCertifiedProductEntity) {
        int colIndex = getColumnIndexMap().getTestTaskStartIndex();
        if (StringUtils.isEmpty(record.get(colIndex))) {
            return 0;
        }

        PendingTestTaskEntity task = new PendingTestTaskEntity();
        task.setUniqueId(record.get(colIndex++).trim());
        if (task.getUniqueId().length() > msgUtil.getMessageAsInteger("maxLength.taskIdentifier")) {
            pendingCertifiedProductEntity.getErrorMessages().add(msgUtil.getMessage("listing.taskIdentifier.maxlength",
                    String.valueOf(msgUtil.getMessageAsInteger("maxLength.taskIdentifier")), task.getUniqueId()));
        }
        task.setDescription(record.get(colIndex++).trim());
        task.setTaskSuccessAverage(record.get(colIndex++).trim());
        task.setTaskSuccessStddev(record.get(colIndex++).trim());
        task.setTaskPathDeviationObserved(record.get(colIndex++).trim());
        task.setTaskPathDeviationOptimal(record.get(colIndex++).trim());
        task.setTaskTimeAvg(record.get(colIndex++).trim());
        task.setTaskTimeStddev(record.get(colIndex++).trim());
        task.setTaskTimeDeviationObservedAvg(record.get(colIndex++).trim());
        task.setTaskTimeDeviationOptimalAvg(record.get(colIndex++).trim());
        task.setTaskErrors(record.get(colIndex++).trim());
        task.setTaskErrorsStddev(record.get(colIndex++).trim());
        task.setTaskRatingScale(record.get(colIndex++).trim());
        task.setTaskRating(record.get(colIndex++).trim());
        task.setTaskRatingStddev(record.get(colIndex++).trim());
        this.tasks.add(task);
        return (getColumnIndexMap().getTestTaskEndIndex() - getColumnIndexMap().getTestTaskStartIndex()) + 1;
    }

    protected int parseCqms(final CSVRecord record, final PendingCertifiedProductEntity pendingCertifiedProduct) {
        int colIndex = getColumnIndexMap().getCqmStartIndex();

        String cqmName = record.get(colIndex++).trim();
        String cqmVersions = record.get(colIndex++).trim();
        String cqmCriteria = record.get(colIndex).trim();

        List<PendingCqmCriterionEntity> criterion = handleCqmCmsCriterion(pendingCertifiedProduct, cqmName, cqmVersions,
                cqmCriteria);
        for (PendingCqmCriterionEntity entity : criterion) {
            if (entity != null && entity.getMappedCriterion() != null && entity.getMeetsCriteria()) {
                pendingCertifiedProduct.getCqmCriterion().add(entity);
            }
        }
        return (getColumnIndexMap().getCqmEndIndex() - getColumnIndexMap().getCqmStartIndex()) + 1;
    }

    protected PendingCertificationResultEntity parseCriteria(
            PendingCertifiedProductEntity pendingCertifiedProduct,
            CSVRecord firstRow, int beginIndex, int endIndex) {
        int currIndex = beginIndex;
        PendingCertificationResultEntity cert = null;
        try {
            String criterionHeading = getHeading().get(currIndex).trim();
            String criterionNumber = getColumnIndexMap().parseCriteriaNumberFromHeading(criterionHeading);
            boolean isCures = getColumnIndexMap().isCriteriaNumberHeadingCures(criterionHeading);
            if (criterionNumber != null && isCures) {
                cert = getCertificationResultCures(criterionNumber, firstRow.get(currIndex++).toString());
            } else if (criterionNumber != null && !isCures) {
                cert = getCertificationResult(criterionNumber, firstRow.get(currIndex++).toString());
            } else {
                LOGGER.warn("Unable to find a criteria for the header value " + criterionHeading);
                pendingCertifiedProduct.getErrorMessages().add("Unable to find a criteria for the header value " + criterionHeading);
                currIndex = (endIndex+1);
            }

            while (currIndex <= endIndex) {
                String colTitle = getHeading().get(currIndex).trim();
                if (!StringUtils.isEmpty(colTitle)) {
                    colTitle = colTitle.trim().toUpperCase();
                    if (colTitle.equalsIgnoreCase(getColumnIndexMap().getGapColumnLabel())) {
                        cert.setGap(asBoolean(firstRow.get(currIndex).trim()));
                        currIndex += getColumnIndexMap().getGapColumnCount();
                    } else if (colTitle
                            .equalsIgnoreCase(getColumnIndexMap().getPrivacySecurityFrameworkColumnLabel())) {
                        cert.setPrivacySecurityFramework(
                                CertificationResult.formatPrivacyAndSecurityFramework(firstRow.get(currIndex)));
                        currIndex += getColumnIndexMap().getPrivacySecurityFrameworkColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getApiDocumentationColumnLabel())) {
                        cert.setApiDocumentation(firstRow.get(currIndex).trim());
                        currIndex += getColumnIndexMap().getApiDocumentationColumnCount();
                    }  else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getExportDocumentationColumnLabel())) {
                        cert.setExportDocumentation(firstRow.get(currIndex).trim());
                        currIndex += getColumnIndexMap().getExportDocumentationColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getAttestationAnswerColumnLabel())) {
                        cert.setAttestationAnswer(asBoolean(firstRow.get(currIndex).trim()));
                        currIndex += getColumnIndexMap().getAttestationAnswerColumnCount();
                    } else if (colTitle
                            .equalsIgnoreCase(getColumnIndexMap().getDocumentationUrlColumnLabel())) {
                        cert.setDocumentationUrl(firstRow.get(currIndex).trim());
                        currIndex += getColumnIndexMap().getDocumentationUrlColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getUseCasesColumnLabel())) {
                        cert.setUseCases(firstRow.get(currIndex).trim());
                        currIndex += getColumnIndexMap().getUseCasesColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestStandardsColumnLabel())) {
                        parseTestStandards(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestStandardsColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestFunctionalityColumnLabel())) {
                        parseTestFunctionality(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestFunctionalityColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getG1MeasureColumnLabel())) {
                        parseG1Measures(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getG1MeasureColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getG2MeasureColumnLabel())) {
                        parseG2Measures(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getG2MeasureColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getAdditionalSoftwareColumnLabel())) {
                        Boolean hasAdditionalSoftware = asBoolean(firstRow.get(currIndex).trim());
                        cert.setHasAdditionalSoftware(hasAdditionalSoftware);
                        parseAdditionalSoftware(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getAdditionalSoftwareColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestToolColumnLabel())) {
                        parseTestTools(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestToolColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestProcedureColumnLabel())) {
                        currIndex += getColumnIndexMap().getTestProcedureColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestProcedureVersionColumnLabel())) {
                        String titleOfPreviousColumn = getHeading().get(currIndex - 1);
                        boolean hasTestProcedureCol = titleOfPreviousColumn
                                .equalsIgnoreCase(getColumnIndexMap().getTestProcedureColumnLabel());
                        parseTestProceduresVersions(cert, hasTestProcedureCol, currIndex);
                        currIndex += getColumnIndexMap().getTestProcedureVersionColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestDataColumnLabel())) {
                        parseTestData(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestDataColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getUcdColumnLabel())) {
                        PendingCertificationResultUcdProcessEntity ucd = new PendingCertificationResultUcdProcessEntity();
                        String ucdName = firstRow.get(currIndex).trim();
                        String ucdDetails = firstRow.get(currIndex + 1).trim();

                        if (!StringUtils.isEmpty(ucdName)) {
                            cert.setSed(Boolean.TRUE);
                            ucd.setUcdProcessName(ucdName);
                            ucd.setUcdProcessDetails(ucdDetails);
                            UcdProcessDTO dto = ucdDao.getByName(ucd.getUcdProcessName());
                            if (dto != null) {
                                ucd.setUcdProcessId(dto.getId());
                            }
                            cert.getUcdProcesses().add(ucd);
                        }
                        currIndex += getColumnIndexMap().getUcdColumnCount();
                    } else if (colTitle.equalsIgnoreCase(getColumnIndexMap().getTestTasksColumnLabel())) {
                        parseTasksAndParticipants(pendingCertifiedProduct, cert, currIndex);
                        currIndex += getColumnIndexMap().getTestTasksColumnCount();
                    } else {
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

    protected void parseTestStandards(final PendingCertifiedProductEntity listing,
            final PendingCertificationResultEntity cert, final int tsColumn) {
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

    protected void parseTestFunctionality(final PendingCertifiedProductEntity listing,
            final PendingCertificationResultEntity cert, final int tfColumn) {
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

    protected void parseAdditionalSoftware(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int asColumnBegin) {
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

        if (cert.getHasAdditionalSoftware() != null && cert.getHasAdditionalSoftware().booleanValue()
                && cert.getAdditionalSoftware().size() == 0) {
            product.getErrorMessages().add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                    + product.getUniqueId() + " indicates additional software should be present but none was found.");
        } else if ((cert.getHasAdditionalSoftware() == null || !cert.getHasAdditionalSoftware().booleanValue())
                && cert.getAdditionalSoftware().size() > 0) {
            product.getErrorMessages()
                    .add("Certification " + cert.getMappedCriterion().getNumber() + " for product "
                            + product.getUniqueId()
                            + " indicates additional software should not be present but some was found.");
        }
    }

    protected void parseTestProceduresVersions(final PendingCertificationResultEntity cert,
            final boolean hasTestProcedureNameCol, final int tpColumn) {
        for (CSVRecord row : getRecord()) {
            String tpValue = row.get(tpColumn).trim();
            if (!StringUtils.isEmpty(tpValue)) {
                PendingCertificationResultTestProcedureEntity tpEntity = new PendingCertificationResultTestProcedureEntity();
                tpEntity.setVersion(tpValue);
                tpEntity.setTestProcedureName(null);
                List<TestProcedureDTO> allowedTestProcedures = testProcedureDao
                        .getByCriterionId(cert.getMappedCriterion().getId());
                if (allowedTestProcedures != null && allowedTestProcedures.size() > 0) {
                    for (TestProcedureDTO allowedTp : allowedTestProcedures) {
                        if (allowedTp.getName().equalsIgnoreCase(TestProcedureDTO.DEFAULT_TEST_PROCEDURE)) {
                            tpEntity.setTestProcedureId(allowedTp.getId());
                        }
                    }
                }

                cert.getTestProcedures().add(tpEntity);
            }
        }
    }

    protected void parseTestData(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int tdColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String tdVersionValue = row.get(tdColumnBegin).trim();
            if (!StringUtils.isEmpty(tdVersionValue)) {
                PendingCertificationResultTestDataEntity tdEntity = new PendingCertificationResultTestDataEntity();
                tdEntity.setTestDataName(null);
                List<TestDataDTO> allowedTestData = testDataDao
                        .getByCriterionId(cert.getMappedCriterion().getId());
                if (allowedTestData != null && allowedTestData.size() > 0) {
                    for (TestDataDTO allowedTd : allowedTestData) {
                        if (allowedTd.getName().equalsIgnoreCase(TestDataDTO.DEFALUT_TEST_DATA)) {
                            tdEntity.setTestDataId(allowedTd.getId());
                        }
                    }
                }
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

    protected void parseTestTools(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int toolColumnBegin) {
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

    protected void parseG1Measures(PendingCertifiedProductEntity listing, PendingCertificationResultEntity cert, int measureCol) {
        // ignore measures for G7
        if (cert.getMappedCriterion().getNumber().equals(G1_CRITERIA_TO_IGNORE)) {
            return;
        }

        MipsMeasurementType type = listingMipsDao.getMeasurementType(G1_MEASUREMENT_TYPE_NAME);
        for (CSVRecord row : getRecord()) {
            String measureVal = row.get(measureCol).trim();
            if (!StringUtils.isEmpty(measureVal) && !measureVal.equalsIgnoreCase(Boolean.FALSE.toString())
                    && !measureVal.equals("0")) {
                //TODO: the parsing of the user-entered measure value field is more complex than this.
                MipsMeasure measureWithName = mipsMeasureDao.getByName(measureVal);
                PendingListingMipsMeasureEntity mmEntity = getMipsMeasureEntityFromListing(listing, measureWithName, type);
                if (mmEntity == null) {
                    mmEntity = new PendingListingMipsMeasureEntity();
                    mmEntity.setValue(measureVal);
                    if (measureWithName != null) {
                        mmEntity.setMeasureId(measureWithName.getId());
                    }
                    mmEntity.setTypeId(type.getId());
                    listing.getMipsMeasures().add(mmEntity);
                }
                PendingListingMipsMeasureCriterionMapEntity criterionMap = new PendingListingMipsMeasureCriterionMapEntity();
                criterionMap.setCertificationCriterionId(cert.getMappedCriterion().getId());
                mmEntity.getAssociatedCriteria().add(criterionMap);
            }
        }
    }

    protected void parseG2Measures(PendingCertifiedProductEntity listing, PendingCertificationResultEntity cert, int measureCol) {
        // ignore measures for G7
        if (cert.getMappedCriterion().getNumber().equals(G2_CRITERIA_TO_IGNORE)) {
            return;
        }

        MipsMeasurementType type = listingMipsDao.getMeasurementType(G2_MEASUREMENT_TYPE_NAME);
        for (CSVRecord row : getRecord()) {
            String measureVal = row.get(measureCol).trim();
            if (!StringUtils.isEmpty(measureVal) && !measureVal.equalsIgnoreCase(Boolean.FALSE.toString())
                    && !measureVal.equals("0")) {
                //TODO: the parsing of the user-entered measure value field is more complex than this.
                MipsMeasure measureWithName = mipsMeasureDao.getByName(measureVal);
                PendingListingMipsMeasureEntity mmEntity = getMipsMeasureEntityFromListing(listing, measureWithName, type);
                if (mmEntity == null) {
                    mmEntity = new PendingListingMipsMeasureEntity();
                    mmEntity.setValue(measureVal);
                    if (measureWithName != null) {
                        mmEntity.setMeasureId(measureWithName.getId());
                    }
                    mmEntity.setTypeId(type.getId());
                    listing.getMipsMeasures().add(mmEntity);
                }
                PendingListingMipsMeasureCriterionMapEntity criterionMap = new PendingListingMipsMeasureCriterionMapEntity();
                criterionMap.setCertificationCriterionId(cert.getMappedCriterion().getId());
                mmEntity.getAssociatedCriteria().add(criterionMap);
            }
        }
    }

    private PendingListingMipsMeasureEntity getMipsMeasureEntityFromListing(PendingCertifiedProductEntity listing,
            MipsMeasure measureToFind, MipsMeasurementType measurementType) {
        Optional<PendingListingMipsMeasureEntity> foundMipsMeasure =
                listing.getMipsMeasures().stream()
                .filter(listingMipsMeasure -> listingMipsMeasure.getId().equals(measureToFind.getId()))
                .filter(listingMipsMeasureSameId ->
                    listingMipsMeasureSameId.getTypeId().equals(measurementType.getId()))
                .findFirst();
        return foundMipsMeasure.isPresent() ? foundMipsMeasure.get() : null;
    }

    protected void parseTasksAndParticipants(final PendingCertifiedProductEntity product,
            final PendingCertificationResultEntity cert, final int taskColumnBegin) {
        for (CSVRecord row : getRecord()) {
            String taskUniqueId = row.get(taskColumnBegin).trim();
            if (!StringUtils.isEmpty(taskUniqueId)) {
                cert.setSed(Boolean.TRUE);
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
                            if (participant.getUniqueId().length() > msgUtil
                                    .getMessageAsInteger("maxLength.participantIdentifier")) {
                                product.getErrorMessages()
                                        .add(msgUtil.getMessage("listing.participantIdentifier.maxlength",
                                                String.valueOf(
                                                        msgUtil.getMessageAsInteger("maxLength.participantIdentifier")),
                                                participant.getUniqueId()));
                            }
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
                            PendingCertificationResultTestTaskParticipantEntity ttPartEntity
                                = new PendingCertificationResultTestTaskParticipantEntity();
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

    /**
     * look up a CQM CMS criteria by number and version. throw an error if we
     * can't find it
     *
     * @param criterionNum
     * @param column
     * @return
     * @throws InvalidArgumentsException
     */
    protected List<PendingCqmCriterionEntity> handleCqmCmsCriterion(final PendingCertifiedProductEntity product,
            String criterionNum, String version, final String mappedCriteria) {
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
                        if (currCriteria.startsWith("170.315")) {
                            currCriteria = currCriteria.split(" ")[1];
                        }
                        CertificationCriterionEntity criteriaEntity = null;
                        try {
                            if (currCriteria.equals("c1") || currCriteria.contentEquals("(c)(1)")) {
                                criteriaEntity = getCriterion("170.315 (c)(1)", false);
                            } else if (currCriteria.equals("c2") || currCriteria.contentEquals("(c)(2)")) {
                                criteriaEntity = getCriterion("170.315 (c)(2)", false);
                            } else if (currCriteria.equals("c3") || currCriteria.contentEquals("(c)(3)")) {
                                criteriaEntity = getCriterion("170.315 (c)(3)", false);
                                //TODO: handle Cures Update version as part of OCD-3149
                            } else if (currCriteria.equals("c4") || currCriteria.contentEquals("(c)(4)")) {
                                criteriaEntity = getCriterion("170.315 (c)(4)", false);
                            }

                            if (criteriaEntity != null) {
                                PendingCqmCertificationCriteriaEntity certEntity = new PendingCqmCertificationCriteriaEntity();
                                certEntity.setCertificationId(criteriaEntity.getId());
                                certEntity.setCertificationCriteria(criteriaEntity);
                                currResult.getCertifications().add(certEntity);
                            } else {
                                product.getErrorMessages().add("Could not find a certification criteria matching "
                                        + currCriteria + " for product " + product.getUniqueId());
                            }
                        } catch (InvalidArgumentsException ex) {
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
