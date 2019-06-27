package gov.healthit.chpl.scheduler.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.scheduler.BrokenSurveillanceRulesDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.scheduler.BrokenSurveillanceRulesDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.DataCollectorAsyncSchedulerHelper;
import gov.healthit.chpl.scheduler.surveillance.rules.RuleComplianceCalculator;

/**
 * Initiates and runs the the Quartz job that generates the data that is used to to create
 * the Broken Surveillance Rules report.
 * @author alarned
 *
 */
@DisallowConcurrentExecution
public class BrokenSurveillanceRulesCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenSurveillanceRulesCreatorJobLogger");
    private static final String EDITION_2011 = "2011";
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private static final Long MILLISECONDS_PER_SECOND = 1000L;
    private static final Long SECONDS_PER_MINUTE = 60L;
    private DateTimeFormatter dateFormatter;
    private Properties props;

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Autowired
    private BrokenSurveillanceRulesDAO brokenSurveillanceRulesDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private DataCollectorAsyncSchedulerHelper dataCollectorAsyncSchedulerHelper;

    @Autowired
    private RuleComplianceCalculator ruleComplianceCalculator;

    /**
     * Constructor to initialize BrokenSurveillanceRulesCreatorJob object.
     * @throws Exception is thrown
     */
    public BrokenSurveillanceRulesCreatorJob() throws Exception {
        super();
        loadProperties();
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        ruleComplianceCalculator.setProps(props);
        dataCollectorAsyncSchedulerHelper.setLogger(LOGGER);

        LOGGER.info("********* Starting the Broken Surveillance Rules Creator job. *********");
        List<CertifiedProductSearchDetails> results = retrieveData();
        List<BrokenSurveillanceRulesDTO> errors = new ArrayList<BrokenSurveillanceRulesDTO>();
        for (CertifiedProductSearchDetails listing : results) {
            errors.addAll(brokenRules(listing));
        }
        LOGGER.info("Deleting {} OBE rules", brokenSurveillanceRulesDAO.findAll().size());
        brokenSurveillanceRulesDAO.deleteAll();
        if (errors.size() > 0) {
            saveBrokenSurveillanceRules(errors);
            LOGGER.info("Saving {} broken rules", errors.size());
        }
        LOGGER.info("********* Completed the Broken Surveillance Rules Creator job. *********");
    }

    private void saveBrokenSurveillanceRules(final List<BrokenSurveillanceRulesDTO> items) {
        try {
            brokenSurveillanceRulesDAO.create(items);
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Unable to save Broken Surveillance Rules {} with error message {}",
                    items.toString(), e.getLocalizedMessage());
        }
    }

    private Properties loadProperties() throws IOException {
        InputStream in =
                BrokenSurveillanceRulesCreatorJob.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
        return props;
    }

    private List<CertifiedProductSearchDetails> retrieveData() {
        List<CertifiedProductFlatSearchResult> listings = certifiedProductSearchDAO.getAllCertifiedProducts();
        List<CertifiedProductFlatSearchResult> certifiedProducts = filterData(listings);
        LOGGER.info("2014/2015 Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(
                certifiedProducts);

        return certifiedProductsWithDetails;
    }

    private List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if ((!result.getEdition().equalsIgnoreCase(EDITION_2011))
                    && (result.getCertificationStatus().equalsIgnoreCase(
                            CertificationStatusType.SuspendedByAcb.getName())
                            || result.getSurveillanceCount() > 0)) {
                results.add(result);
            }
        }
        return results;
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {

        List<CertifiedProductSearchDetails> details = new ArrayList<CertifiedProductSearchDetails>();
        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();

        for (CertifiedProductFlatSearchResult certifiedProduct : certifiedProducts) {
            try {
                futures.add(dataCollectorAsyncSchedulerHelper
                        .getCertifiedProductDetail(certifiedProduct.getId(), certifiedProductDetailsManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + certifiedProduct.getId(), e);
            }
        }

        Date startTime = new Date();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                details.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Could not retrieve certified product details for unknown id.", e);
            }
        }

        Date endTime = new Date();
        long seconds = (endTime.getTime() - startTime.getTime()) / MILLISECONDS_PER_SECOND;
        long minutes = seconds / SECONDS_PER_MINUTE;
        LOGGER.info("Time to retrieve details: {} seconds or {} minutes", seconds, minutes);

        return details;
    }

    private List<BrokenSurveillanceRulesDTO> brokenRules(final CertifiedProductSearchDetails listing) {
        List<BrokenSurveillanceRulesDTO> errors = new ArrayList<BrokenSurveillanceRulesDTO>();

        if (listing.getSurveillance().size() == 0) {
            BrokenSurveillanceRulesDTO rule = getDefaultBrokenRule(listing);
            if (!StringUtils.isEmpty(rule.getLengthySuspensionRule())) {
                errors.add(rule);
            }
        } else {
            for (Surveillance surv : listing.getSurveillance()) {
                boolean foundBrokenNc = false;
                for (SurveillanceRequirement req : surv.getRequirements()) {
                    for (SurveillanceNonconformity nc : req.getNonconformities()) {
                        if (nc.getStatus().getName().equalsIgnoreCase(SurveillanceNonconformityStatus.OPEN)) {
                            boolean ncHasError = false;
                            BrokenSurveillanceRulesDTO rule = getDefaultBrokenRule(listing);
                            rule = addSurveillanceData(rule, surv);
                            rule = addNcData(rule, nc);
                            List<OversightRuleResult> oversightResult =
                                    ruleComplianceCalculator.calculateCompliance(listing, surv, nc);
                            for (OversightRuleResult currResult : oversightResult) {
                                String dateBrokenStr = "";
                                if (currResult.getDateBroken() != null) {
                                    LocalDateTime dateBroken = LocalDateTime
                                            .ofInstant(Instant.ofEpochMilli(
                                                    currResult.getDateBroken().getTime()), ZoneId.systemDefault());
                                    dateBrokenStr = dateFormatter.format(dateBroken);

                                    switch (currResult.getRule()) {
                                    case CAP_NOT_APPROVED:
                                        rule.setCapNotApprovedRule(dateBrokenStr);
                                        ncHasError = true;
                                        break;
                                    case CAP_NOT_STARTED:
                                        rule.setCapNotStartedRule(dateBrokenStr);
                                        ncHasError = true;
                                        break;
                                    case CAP_NOT_COMPLETED:
                                        rule.setCapNotCompletedRule(dateBrokenStr);
                                        ncHasError = true;
                                        break;
                                    case CAP_NOT_CLOSED:
                                        rule.setCapNotClosedRule(dateBrokenStr);
                                        ncHasError = true;
                                        break;
                                    case NONCONFORMITY_OPEN_CAP_COMPLETE:
                                        rule.setClosedCapWithOpenNonconformityRule(dateBrokenStr);
                                        ncHasError = true;
                                        break;
                                    case LONG_SUSPENSION:
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            }
                            if (ncHasError) {
                                rule.setNonconformityStatus(nc.getStatus().getName());
                                foundBrokenNc = true;
                                errors.add(rule);
                            }
                        }
                    }
                    if (!foundBrokenNc) {
                        BrokenSurveillanceRulesDTO rule = getDefaultBrokenRule(listing);
                        rule = addSurveillanceData(rule, surv);
                        if (!StringUtils.isEmpty(rule.getLengthySuspensionRule())) {
                            errors.add(rule);
                        }
                    }
                }
            }
        }
        return errors;
    }

    private BrokenSurveillanceRulesDTO getDefaultBrokenRule(final CertifiedProductSearchDetails listing) {
        BrokenSurveillanceRulesDTO base = new BrokenSurveillanceRulesDTO();
        base.setDeveloper(listing.getDeveloper().getName());
        base.setProduct(listing.getProduct().getName());
        base.setVersion(listing.getVersion().getVersion());
        base.setAcb(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        base.setChplProductNumber(listing.getChplProductNumber());
        String productDetailsUrl = props.getProperty("chplUrlBegin").trim();
        if (!productDetailsUrl.endsWith("/")) {
            productDetailsUrl += "/";
        }
        productDetailsUrl += "#/product/" + listing.getId();
        base.setUrl(productDetailsUrl);

        base.setCertificationStatus(listing.getCurrentStatus().getStatus().getName());
        Long lastCertificationChangeMillis = listing.getCurrentStatus().getEventDate();
        if (lastCertificationChangeMillis.longValue() == listing.getCertificationDate().longValue()) {
            base.setDateOfLastStatusChange("No status change");
        } else {
            LocalDateTime lastStatusChangeDate = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(lastCertificationChangeMillis), ZoneId.systemDefault());
            base.setDateOfLastStatusChange(dateFormatter.format(lastStatusChangeDate));
        }
        List<OversightRuleResult> result = ruleComplianceCalculator.calculateCompliance(listing, null, null);
        if (result != null && result.size() > 0 && result.get(0).getDateBroken() != null) {
            LocalDateTime dateBroken = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(result.get(0).getDateBroken().getTime()), ZoneId.systemDefault());
            base.setLengthySuspensionRule(dateFormatter.format(dateBroken));
        }
        return base;
    }

    private BrokenSurveillanceRulesDTO addSurveillanceData(
            final BrokenSurveillanceRulesDTO rule, final Surveillance surv) {

        if (surv.getFriendlyId() != null) {
            rule.setSurveillanceId(surv.getFriendlyId());
        }
        if (surv.getStartDate() != null) {
            LocalDateTime survStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getStartDate().getTime()),
                    ZoneId.systemDefault());
            rule.setDateSurveillanceBegan(dateFormatter.format(survStartDate));
        }
        if (surv.getEndDate() != null) {
            LocalDateTime survEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(surv.getEndDate().getTime()),
                    ZoneId.systemDefault());
            rule.setDateSurveillanceEnded(dateFormatter.format(survEndDate));
        }
        rule.setSurveillanceType(surv.getType().getName());
        rule.setNonconformity(false);
        return rule;
    }

    private BrokenSurveillanceRulesDTO addNcData(
            final BrokenSurveillanceRulesDTO rule, final SurveillanceNonconformity nc) {

        rule.setNonconformity(true);
        rule.setNonconformityCriteria(nc.getNonconformityType());
        LocalDateTime ncDeterminationDate = null;
        if (nc.getDateOfDetermination() != null) {
            ncDeterminationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getDateOfDetermination().getTime()),
                    ZoneId.systemDefault());
            rule.setDateOfDeterminationOfNonconformity(dateFormatter.format(ncDeterminationDate));
        }
        LocalDateTime capApprovalDate = null;
        if (nc.getCapApprovalDate() != null) {
            capApprovalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()),
                    ZoneId.systemDefault());
            rule.setCorrectiveActionPlanApprovedDate(dateFormatter.format(capApprovalDate));
        }
        LocalDateTime capStartDate = null;
        if (nc.getCapStartDate() != null) {
            capStartDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapStartDate().getTime()),
                    ZoneId.systemDefault());
            rule.setDateCorrectiveActionBegan(dateFormatter.format(capStartDate));
        }
        LocalDateTime capMustCompleteDate = null;
        if (nc.getCapMustCompleteDate() != null) {
            capMustCompleteDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapMustCompleteDate().getTime()),
                    ZoneId.systemDefault());
            rule.setDateCorrectiveActionMustBeCompleted(dateFormatter.format(capMustCompleteDate));
        }
        LocalDateTime capEndDate = null;
        if (nc.getCapEndDate() != null) {
            capEndDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(nc.getCapEndDate().getTime()),
                    ZoneId.systemDefault());
            rule.setDateCorrectiveActionWasCompleted(dateFormatter.format(capEndDate));
        }

        if (capApprovalDate != null) {
            Duration timeBetween = Duration.between(ncDeterminationDate, capApprovalDate);
            rule.setNumberOfDaysFromDeterminationToCapApproval(timeBetween.toDays());
        } else {
            Duration timeBetween = Duration.between(ncDeterminationDate, LocalDateTime.now());
            rule.setNumberOfDaysFromDeterminationToPresent(timeBetween.toDays());
        }

        if (capApprovalDate != null && capStartDate != null) {
            Duration timeBetween = Duration.between(capApprovalDate, capStartDate);
            rule.setNumberOfDaysFromCapApprovalToCapBegan(timeBetween.toDays());
        } else if (capApprovalDate != null) {
            Duration timeBetween = Duration.between(capApprovalDate, LocalDateTime.now());
            rule.setNumberOfDaysFromCapApprovalToPresent(timeBetween.toDays());
        }

        if (capStartDate != null && capEndDate != null) {
            Duration timeBetween = Duration.between(capStartDate, capEndDate);
            rule.setNumberOfDaysFromCapBeganToCapCompleted(timeBetween.toDays());
        } else if (capStartDate != null) {
            Duration timeBetween = Duration.between(capStartDate, LocalDateTime.now());
            rule.setNumberOfDaysFromCapBeganToPresent(timeBetween.toDays());
        }

        if (capEndDate != null && capMustCompleteDate != null) {
            Duration timeBetween = Duration.between(capMustCompleteDate, capEndDate);
            rule.setDifferenceFromCapCompletedAndCapMustBeCompleted(timeBetween.toDays());
        }
        return rule;
    }
}
