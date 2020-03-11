package gov.healthit.chpl.upload.surveillance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;

@Component("surveillanceUploadHandler2015")
public class SurveillanceUploadHandler2015 implements SurveillanceUploadHandler {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceUploadHandler2015.class);

    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String FIRST_ROW_REGEX = "^NEW|UPDATE$";
    private static final String SUBSEQUENT_ROW = "SUBELEMENT";

    private CertifiedProductDAO cpDao;
    private CertificationResultDetailsDAO certResultDetailsDao;
    private SurveillanceDAO survDao;

    protected SimpleDateFormat dateFormatter;
    private List<CSVRecord> record;
    private CSVRecord heading;
    private int lastDataIndex;

    @Autowired
    public SurveillanceUploadHandler2015(CertifiedProductDAO cpDao, SurveillanceDAO survDao,
            CertificationResultDetailsDAO certResultDetailsDao) {
        this.cpDao = cpDao;
        this.survDao = survDao;
        this.certResultDetailsDao = certResultDetailsDao;
        dateFormatter = new SimpleDateFormat(DATE_FORMAT);
    }

    @Transactional(readOnly = true)
    public Surveillance handle() throws InvalidArgumentsException {
        Surveillance surv = new Surveillance();

        // get things that are only in the first row of the surveillance
        for (CSVRecord csvRecord : getRecord()) {
            String statusStr = csvRecord.get(0).trim();
            if (!StringUtils.isEmpty(statusStr) && statusStr.toUpperCase().matches(FIRST_ROW_REGEX)) {
                parseSurveillanceDetails(csvRecord, surv, statusStr.equalsIgnoreCase("UPDATE"));
            }
        }

        List<CertificationResultDetailsDTO> certResults =
                certResultDetailsDao.getCertificationResultsForSurveillanceListing(surv);

        // if we got errors here, don't parse the rest of it
        if (surv.getErrorMessages() == null || surv.getErrorMessages().size() == 0) {
            // get surveilled requirements
            for (CSVRecord csvRecord : getRecord()) {
                String statusStr = csvRecord.get(0).trim();
                if (!StringUtils.isEmpty(statusStr) && (statusStr.toUpperCase().matches(FIRST_ROW_REGEX)
                        || statusStr.toUpperCase().equalsIgnoreCase(SUBSEQUENT_ROW))) {
                    parseSurveilledRequirements(csvRecord, surv, certResults);
                }
            }

            // get nonconformities
            for (CSVRecord csvRecord : getRecord()) {
                String statusStr = csvRecord.get(0).trim();
                if (!StringUtils.isEmpty(statusStr) && (statusStr.toUpperCase().matches(FIRST_ROW_REGEX)
                        || statusStr.toUpperCase().equalsIgnoreCase(SUBSEQUENT_ROW))) {
                    parseNonconformities(csvRecord, surv, certResults);
                }
            }
        }

        return surv;
    }

    public void parseSurveillanceDetails(CSVRecord record, Surveillance surv, boolean isUpdate) {
        int colIndex = 1;

        // find the chpl product this surveillance will be attached to
        String chplId = record.get(colIndex++).trim();
        if (chplId.startsWith("CHP-")) {
            try {
                CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplId);
                if (chplProduct != null) {
                    CertifiedProductDetailsDTO chplProductDetails = cpDao.getDetailsById(chplProduct.getId());
                    if (chplProductDetails != null) {
                        surv.setCertifiedProduct(new CertifiedProduct(chplProductDetails));
                    } else {
                        LOGGER.error("Found chpl product with product id '" + chplId
                                + "' but could not find certified product with id '" + chplProduct.getId() + "'.");
                    }
                } else {
                    LOGGER.error("Could not find chpl product with product id '" + chplId + "'.");
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Exception looking up CHPL product details for '" + chplId + "'.");
            }
        } else {
            try {
                CertifiedProductDetailsDTO chplProductDetails = cpDao.getByChplUniqueId(chplId);
                if (chplProductDetails != null) {
                    surv.setCertifiedProduct(new CertifiedProduct(chplProductDetails));
                } else {
                    LOGGER.error("Could not find chpl product with unique id '" + chplId + "'.");
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Exception looking up " + chplId, ex);
            }
        }

        if (surv.getCertifiedProduct() == null || surv.getCertifiedProduct().getId() == null) {
            surv.getErrorMessages().add("Could not find Certified Product with unique id " + chplId);
            return;
        }

        // find the surveillance id in case this is an update
        String survFriendlyId = record.get(colIndex++).trim();
        if (isUpdate && StringUtils.isEmpty(survFriendlyId)) {
            LOGGER.error("Surveillance UPDATE specified but no surveillance ID was found");
            surv.getErrorMessages()
                    .add("No surveillance ID was specified for surveillance update on certified product " + chplId);
            return;
        } else if (isUpdate
                && survDao.getSurveillanceByCertifiedProductAndFriendlyId(surv.getCertifiedProduct().getId(),
                        survFriendlyId) == null) {
            LOGGER.error("Could not find existing surveillance for update with certified product id "
                    + surv.getCertifiedProduct().getId() + " and friendly id " + survFriendlyId);
            surv.getErrorMessages().add("No surveillance exists for certified product "
                    + surv.getCertifiedProduct().getChplProductNumber() + " and surveillance id " + survFriendlyId);
            return;
        }
        surv.setSurveillanceIdToReplace(survFriendlyId);

        // surveillance begin date
        String beginDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(beginDateStr)) {
            try {
                Date beginDate = dateFormatter.parse(beginDateStr);
                surv.setStartDate(beginDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse begin date '" + beginDateStr + "'.");
            }
        }

        // surveillance end date
        String endDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(endDateStr)) {
            try {
                Date endDate = dateFormatter.parse(endDateStr);
                surv.setEndDate(endDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse end date '" + endDateStr + "'.");
            }
        }

        // surveillance type
        String survTypeStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(survTypeStr)) {
            SurveillanceType type = new SurveillanceType();
            type.setName(survTypeStr);
            surv.setType(type);
        }

        // randomized sites num used
        String randomizedSitesUsedStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(randomizedSitesUsedStr)) {
            try {
                Integer randomizedSitesUsed = Integer.parseInt(randomizedSitesUsedStr);
                surv.setRandomizedSitesUsed(randomizedSitesUsed);
            } catch (Exception ex) {
                LOGGER.error("Could not parse '" + randomizedSitesUsedStr + "' as an integer.");
            }
        }
    }

    public void parseSurveilledRequirements(CSVRecord csvRecord, Surveillance surv,
            List<CertificationResultDetailsDTO> certResults) {
        SurveillanceRequirement req = new SurveillanceRequirement();

        int colIndex = 7;
        // requirement type
        String requirementTypeStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(requirementTypeStr)) {
            SurveillanceRequirementType requirementType = new SurveillanceRequirementType();
            requirementType.setName(requirementTypeStr);
            req.setType(requirementType);
        }

        // the requirement
        String requirementStr = csvRecord.get(colIndex++).trim();
        req.setRequirement(requirementStr);
        //look at cert results to determine if/which criterion
        if (certResults != null && certResults.size() > 0) {
            Optional<CertificationResultDetailsDTO> attestedCertResult =
                    certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, req.getRequirement()))
                    .findFirst();
            if (attestedCertResult.isPresent()) {
                req.setCriterion(new CertificationCriterion(attestedCertResult.get().getCriterion()));
            }
        }

        // surveillance result
        String resultTypeStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(resultTypeStr)) {
            SurveillanceResultType resultType = new SurveillanceResultType();
            resultType.setName(resultTypeStr);
            req.setResult(resultType);
        }
        surv.getRequirements().add(req);
    }

    public void parseNonconformities(CSVRecord record, Surveillance surv, List<CertificationResultDetailsDTO> certResults) {
        int ncBeginIndex = 10;
        // if the first nonconformity cell is blank, forget it
        if (StringUtils.isEmpty(record.get(ncBeginIndex).trim())) {
            return;
        }

        // find the requirement this nonconformity belongs to
        int colIndex = 8;
        SurveillanceRequirement req = null;
        String requirementStr = record.get(colIndex).trim();
        if (!StringUtils.isEmpty(requirementStr)) {
            Iterator<SurveillanceRequirement> reqIter = surv.getRequirements().iterator();
            while (reqIter.hasNext() && req == null) {
                SurveillanceRequirement currReq = reqIter.next();
                if (currReq.getRequirement() != null && currReq.getRequirement().equalsIgnoreCase(requirementStr)) {
                    req = currReq;
                }
            }
        }

        // parse out all nonconformity data
        colIndex = 10;
        SurveillanceNonconformity nc = new SurveillanceNonconformity();

        // nonconformity type
        String ncTypeStr = record.get(colIndex++).trim();
        nc.setNonconformityType(ncTypeStr);
        //look at cert results to determine if/which criterion
        if (certResults != null && certResults.size() > 0) {
            Optional<CertificationResultDetailsDTO> attestedCertResult =
                    certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, nc.getNonconformityType()))
                    .findFirst();
            if (attestedCertResult.isPresent()) {
                nc.setCriterion(new CertificationCriterion(attestedCertResult.get().getCriterion()));
            }
        }

        // nonconformity status
        String ncStatusStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(ncStatusStr)) {
            SurveillanceNonconformityStatus ncStatus = new SurveillanceNonconformityStatus();
            ncStatus.setName(ncStatusStr);
            nc.setStatus(ncStatus);
        }

        // date of determination
        String determinationDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(determinationDateStr)) {
            try {
                Date determinationDate = dateFormatter.parse(determinationDateStr);
                nc.setDateOfDetermination(determinationDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse determination date '" + determinationDateStr + "'.");
            }
        }

        // CAP approval date
        String capApprovalDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(capApprovalDateStr)) {
            try {
                Date capApprovalDate = dateFormatter.parse(capApprovalDateStr);
                nc.setCapApprovalDate(capApprovalDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse CAP approval date '" + capApprovalDateStr + "'.");
            }
        }

        // action begin date
        String actionBeginDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(actionBeginDateStr)) {
            try {
                Date actionBeginDate = dateFormatter.parse(actionBeginDateStr);
                nc.setCapStartDate(actionBeginDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse action begin date '" + actionBeginDateStr + "'.");
            }
        }

        // must complete date
        String mustCompleteDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(mustCompleteDateStr)) {
            try {
                Date mustCompleteDate = dateFormatter.parse(mustCompleteDateStr);
                nc.setCapMustCompleteDate(mustCompleteDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse must complete date '" + mustCompleteDateStr + "'.");
            }
        }

        // was complete date
        String wasCompleteDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(wasCompleteDateStr)) {
            try {
                Date wasCompleteDate = dateFormatter.parse(wasCompleteDateStr);
                nc.setCapEndDate(wasCompleteDate);
            } catch (final ParseException pex) {
                LOGGER.error("Could not parse was complete date '" + wasCompleteDateStr + "'.");
            }
        }

        // summary
        String summary = record.get(colIndex++).trim();
        nc.setSummary(summary);

        // findings
        String findings = record.get(colIndex++).trim();
        nc.setFindings(findings);

        // sites passed
        String sitesPassedStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(sitesPassedStr)) {
            try {
                Integer sitesPassed = Integer.parseInt(sitesPassedStr);
                nc.setSitesPassed(sitesPassed);
            } catch (Exception ex) {
                LOGGER.error("Could not parse '" + sitesPassedStr + "' as an integer.");
            }
        }

        // total sites used
        String totalSitesUsedStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(totalSitesUsedStr)) {
            try {
                Integer totalSitesUsed = Integer.parseInt(totalSitesUsedStr);
                nc.setTotalSites(totalSitesUsed);
            } catch (Exception ex) {
                LOGGER.error("Could not parse '" + totalSitesUsedStr + "' as an integer.");
            }
        }

        // developer explanation
        String devExplanationStr = record.get(colIndex++).trim();
        nc.setDeveloperExplanation(devExplanationStr);

        // resolution
        String resolutionStr = record.get(colIndex++).trim();
        nc.setResolution(resolutionStr);

        if (req != null) {
            req.getNonconformities().add(nc);
        }
    }

    private boolean isCriteriaAttestedTo(CertificationResultDetailsDTO certResult, String criterionNumber) {
        return !StringUtils.isEmpty(certResult.getNumber())
                && certResult.getSuccess() != null
                && certResult.getSuccess().booleanValue()
                && certResult.getNumber().equals(criterionNumber);
    }
    public List<CSVRecord> getRecord() {
        return record;
    }

    public void setRecord(final List<CSVRecord> record) {
        this.record = record;
    }

    public CSVRecord getHeading() {
        return heading;
    }

    public void setHeading(final CSVRecord heading) {
        this.heading = heading;
    }

    public int getLastDataIndex() {
        return lastDataIndex;
    }

    public void setLastDataIndex(final int lastDataIndex) {
        this.lastDataIndex = lastDataIndex;
    }
}
