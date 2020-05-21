package gov.healthit.chpl.scheduler.job;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import gov.healthit.chpl.scheduler.surveillance.rules.RuleComplianceCalculator;

@DisallowConcurrentExecution
public class BrokenSurveillanceRulesCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenSurveillanceRulesCreatorJobLogger");
    private static final String EDITION_2011 = "2011";
    private DateTimeFormatter dateFormatter;

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Autowired
    private BrokenSurveillanceRulesDAO brokenSurveillanceRulesDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private RuleComplianceCalculator ruleComplianceCalculator;

    @Autowired
    private Environment env;

    public BrokenSurveillanceRulesCreatorJob() throws Exception {
        super();
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Broken Surveillance Rules Creator job. *********");
        ExecutorService executorService = null;
        try {
            deleteAllExistingBrokenSurveillanceRules();

            executorService = Executors.newFixedThreadPool(getThreadCountForJob());
            List<CertifiedProductFlatSearchResult> listingsForReport = getListingsForReport();
            LOGGER.info(String.format("Found %s listings to process", listingsForReport.size()));

            for (CertifiedProductFlatSearchResult listing : listingsForReport) {
                CompletableFuture.runAsync(() -> processListing(listing.getId()), executorService);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }

        LOGGER.info("********* Completed the Broken Surveillance Rules Creator job. *********");
    }

    private List<CertifiedProductFlatSearchResult> getListingsForReport() {
        return certifiedProductSearchDAO.getAllCertifiedProducts().stream()
                .filter(listing -> !isEdition2011(listing)
                        && (isCertificationStatusSuspendedByAcb(listing)
                                || hasSurveillances(listing)))
                .collect(Collectors.toList());
    }

    private boolean isEdition2011(CertifiedProductFlatSearchResult listing) {
        return listing.getEdition().equals(EDITION_2011);
    }

    private boolean isCertificationStatusSuspendedByAcb(CertifiedProductFlatSearchResult listing) {
        return listing.getCertificationStatus().equalsIgnoreCase(CertificationStatusType.SuspendedByAcb.getName());
    }

    private boolean hasSurveillances(CertifiedProductFlatSearchResult listing) {
        return listing.getSurveillanceCount() > 0;
    }

    private void processListing(Long listingId) {
        CertifiedProductSearchDetails listing;
        try {
            LOGGER.info(String.format("Retrieving CertifiedProductDetails for: %s", listingId));
            listing = certifiedProductDetailsManager.getCertifiedProductDetails(listingId);
            LOGGER.info(String.format("Complete retrieving CertifiedProductDetails for: %s", listingId));
            saveBrokenSurveillanceRules(brokenRules(listing), listingId);
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not process listingId: %s  Reason: %s", listingId, e.getMessage()), e);
        }

    }

    private List<BrokenSurveillanceRulesDTO> brokenRules(CertifiedProductSearchDetails listing) {
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
                            List<OversightRuleResult> oversightResult = ruleComplianceCalculator
                                    .calculateCompliance(listing, surv, nc);
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

    private BrokenSurveillanceRulesDTO getDefaultBrokenRule(CertifiedProductSearchDetails listing) {
        BrokenSurveillanceRulesDTO base = new BrokenSurveillanceRulesDTO();
        base.setDeveloper(listing.getDeveloper().getName());
        base.setProduct(listing.getProduct().getName());
        base.setVersion(listing.getVersion().getVersion());
        base.setAcb(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
        base.setChplProductNumber(listing.getChplProductNumber());
        String productDetailsUrl = env.getProperty("chplUrlBegin").trim();
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

    private BrokenSurveillanceRulesDTO addSurveillanceData(BrokenSurveillanceRulesDTO rule, Surveillance surv) {

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

    private BrokenSurveillanceRulesDTO addNcData(BrokenSurveillanceRulesDTO rule, SurveillanceNonconformity nc) {

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

    private void saveBrokenSurveillanceRules(List<BrokenSurveillanceRulesDTO> items, Long listingId) {
        try {
            LOGGER.info(String.format("Saving %s Broken Surveillance Rules for listing: %s", items.size(), listingId));
            brokenSurveillanceRulesDAO.create(items);
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Unable to save Broken Surveillance Rules {} with error message {}",
                    items.toString(), e.getLocalizedMessage());
        }
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private void deleteAllExistingBrokenSurveillanceRules() {
        LOGGER.info("Deleting {} OBE rules", brokenSurveillanceRulesDAO.findAll().size());
        brokenSurveillanceRulesDAO.deleteAll();
    }

}
