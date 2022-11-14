package gov.healthit.chpl.upload.surveillance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component("surveillanceUploadHandler2015")
public class SurveillanceUploadHandler2015 implements SurveillanceUploadHandler {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceUploadHandler2015.class);

    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String FIRST_ROW_REGEX = "^NEW|UPDATE$";
    private static final String SUBSEQUENT_ROW = "SUBELEMENT";

    private CertifiedProductDAO cpDao;
    private SurveillanceDAO survDao;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    private List<RequirementType> requirementTypes;
    private List<NonconformityType> nonconformityTypes;

    protected DateTimeFormatter dateFormatter;
    private List<CSVRecord> record;
    private CSVRecord heading;
    private int lastDataIndex;

    @Autowired
    public SurveillanceUploadHandler2015(CertifiedProductDAO cpDao, SurveillanceDAO survDao,
            CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.cpDao = cpDao;
        this.survDao = survDao;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        requirementTypes = survDao.getRequirementTypes();
        nonconformityTypes = survDao.getNonconformityTypes();
    }

    @Override
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

        // if we got errors here, don't parse the rest of it
        if (surv.getErrorMessages() == null || surv.getErrorMessages().size() == 0) {
            // get surveilled requirements
            for (CSVRecord csvRecord : getRecord()) {
                String statusStr = csvRecord.get(0).trim();
                if (!StringUtils.isEmpty(statusStr) && (statusStr.toUpperCase().matches(FIRST_ROW_REGEX)
                        || statusStr.toUpperCase().equalsIgnoreCase(SUBSEQUENT_ROW))) {
                    parseSurveilledRequirements(csvRecord, surv);
                }
            }

            // get nonconformities
            for (CSVRecord csvRecord : getRecord()) {
                String statusStr = csvRecord.get(0).trim();
                if (!StringUtils.isEmpty(statusStr) && (statusStr.toUpperCase().matches(FIRST_ROW_REGEX)
                        || statusStr.toUpperCase().equalsIgnoreCase(SUBSEQUENT_ROW))) {
                    parseNonconformities(csvRecord, surv);
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
                surv.setStartDay(LocalDate.parse(beginDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse begin date '" + beginDateStr + "'.");
            }
        }

        // surveillance end date
        String endDateStr = record.get(colIndex++).trim();
        if (!StringUtils.isEmpty(endDateStr)) {
            try {
                surv.setEndDay(LocalDate.parse(endDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
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

    public void parseSurveilledRequirements(CSVRecord csvRecord, Surveillance surv) {
        SurveillanceRequirement req = new SurveillanceRequirement();

        int colIndex = 7;
        // requirement detail type
        //Need to use the listing to determine if the requirementType (when it's a Certified Capability)
        //relates to the "old" or the "Cures" version of the criterion.  When requirementType is a Certified Capability
        //the requirementType.id is the cert criterion id.
        String requirementTypeStr = csvRecord.get(colIndex++).trim();
        String requirementStr = csvRecord.get(colIndex++).trim();
        req.setRequirementType(requirementTypes.stream()
                .filter(detailType -> ((NullSafeEvaluator.eval(() -> detailType.getNumber(), "") .equals(requirementStr)
                                        && isCriterionAttestedTo(surv.getCertifiedProduct().getId(), detailType.getId()))
                        || NullSafeEvaluator.eval(() -> detailType.getTitle(), "") .equals(requirementStr))
                        && detailType.getRequirementGroupType().getName().equals(requirementTypeStr))
                .findAny()
                .orElse(null));

        // surveillance result
        String resultTypeStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(resultTypeStr)) {
            SurveillanceResultType resultType = new SurveillanceResultType();
            resultType.setName(resultTypeStr);
            req.setResult(resultType);
        }
        surv.getRequirements().add(req);

    }

    public void parseNonconformities(CSVRecord csvRecord, Surveillance surv) {
        int ncBeginIndex = 10;
        // if the first nonconformity cell is blank, forget it
        if (StringUtils.isEmpty(csvRecord.get(ncBeginIndex).trim())) {
            return;
        }

        // find the requirement this nonconformity belongs to
        int colIndex = 8;
        String requirementStr = csvRecord.get(colIndex).trim();
        SurveillanceRequirement req = surv.getRequirements().stream()
                .filter(r -> NullSafeEvaluator.eval(() -> r.getRequirementType().getNumber(), "").equals(requirementStr)
                        || NullSafeEvaluator.eval(() -> r.getRequirementType().getTitle(), "").equals(requirementStr))
                .findAny()
                .orElse(null);

        // parse out all nonconformity data
        colIndex = 10;
        SurveillanceNonconformity nc = new SurveillanceNonconformity();

        // nonconformity type
        //Need to use the listing to determine if the nonconformityType (when it's a Certified Capability)
        //relates to the "old" or the "Cures" version of the criterion.  When nonconformityType is a Certified
        //Capability the nc.id is the cert criterion id.
        String ncTypeStr = csvRecord.get(colIndex++).trim();
        nc.setType(nonconformityTypes.stream()
                .filter(ncType -> (NullSafeEvaluator.eval(() -> ncType.getNumber(), "").equals(ncTypeStr)
                                    && isCriterionAttestedTo(surv.getCertifiedProduct().getId(), ncType.getId()))
                        || NullSafeEvaluator.eval(() -> ncType.getTitle(), "").equals(ncTypeStr))
                .findAny()
                .orElse(null));

        // nonconformity status
        String ncStatusStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(ncStatusStr)) {
            nc.setNonconformityStatus(ncStatusStr);
        }

        // date of determination
        String determinationDateStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(determinationDateStr)) {
            try {
                nc.setDateOfDeterminationDay(LocalDate.parse(determinationDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse determination date '" + determinationDateStr + "'.");
            }
        }

        // CAP approval date
        String capApprovalDateStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(capApprovalDateStr)) {
            try {
                nc.setCapApprovalDay(LocalDate.parse(capApprovalDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse CAP approval date '" + capApprovalDateStr + "'.");
            }
        }

        // action begin date
        String actionBeginDateStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(actionBeginDateStr)) {
            try {
                nc.setCapStartDay(LocalDate.parse(actionBeginDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse action begin date '" + actionBeginDateStr + "'.");
            }
        }

        // must complete date
        String mustCompleteDateStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(mustCompleteDateStr)) {
            try {
                nc.setCapMustCompleteDay(LocalDate.parse(mustCompleteDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse must complete date '" + mustCompleteDateStr + "'.");
            }
        }

        // was complete date
        String wasCompleteDateStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(wasCompleteDateStr)) {
            try {
                nc.setCapEndDay(LocalDate.parse(wasCompleteDateStr, dateFormatter));
            } catch (final DateTimeParseException pex) {
                LOGGER.error("Could not parse was complete date '" + wasCompleteDateStr + "'.");
            }
        }

        // summary
        String summary = csvRecord.get(colIndex++).trim();
        nc.setSummary(summary);

        // findings
        String findings = csvRecord.get(colIndex++).trim();
        nc.setFindings(findings);

        // sites passed
        String sitesPassedStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(sitesPassedStr)) {
            try {
                Integer sitesPassed = Integer.parseInt(sitesPassedStr);
                nc.setSitesPassed(sitesPassed);
            } catch (Exception ex) {
                LOGGER.error("Could not parse '" + sitesPassedStr + "' as an integer.");
            }
        }

        // total sites used
        String totalSitesUsedStr = csvRecord.get(colIndex++).trim();
        if (!StringUtils.isEmpty(totalSitesUsedStr)) {
            try {
                Integer totalSitesUsed = Integer.parseInt(totalSitesUsedStr);
                nc.setTotalSites(totalSitesUsed);
            } catch (Exception ex) {
                LOGGER.error("Could not parse '" + totalSitesUsedStr + "' as an integer.");
            }
        }

        // developer explanation
        String devExplanationStr = csvRecord.get(colIndex++).trim();
        nc.setDeveloperExplanation(devExplanationStr);

        // resolution
        String resolutionStr = csvRecord.get(colIndex++).trim();
        nc.setResolution(resolutionStr);

        if (req != null) {
            req.getNonconformities().add(nc);
        }
    }

    private Boolean isCriterionAttestedTo(Long listingId, Long criterionId) {
        try {
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
            return listing.getCertificationResults().stream()
                    .filter(cr -> cr.isSuccess() && cr.getCriterion().getId().equals(criterionId))
                    .findAny()
                    .isPresent();
        } catch (EntityRetrievalException e) {
            return null;
        }
    }

    @Override
    public List<CSVRecord> getRecord() {
        return record;
    }

    @Override
    public void setRecord(final List<CSVRecord> record) {
        this.record = record;
    }

    @Override
    public CSVRecord getHeading() {
        return heading;
    }

    @Override
    public void setHeading(final CSVRecord heading) {
        this.heading = heading;
    }

    @Override
    public int getLastDataIndex() {
        return lastDataIndex;
    }

    @Override
    public void setLastDataIndex(final int lastDataIndex) {
        this.lastDataIndex = lastDataIndex;
    }
}
