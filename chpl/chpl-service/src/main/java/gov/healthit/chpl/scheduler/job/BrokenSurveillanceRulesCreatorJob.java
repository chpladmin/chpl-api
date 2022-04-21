package gov.healthit.chpl.scheduler.job;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.scheduler.BrokenSurveillanceRulesDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.scheduler.BrokenSurveillanceRulesDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.surveillance.rules.RuleComplianceCalculator;
import gov.healthit.chpl.search.dao.ListingSearchDao;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;

@DisallowConcurrentExecution
public class BrokenSurveillanceRulesCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenSurveillanceRulesCreatorJobLogger");
    private static final String EDITION_2015 = "2015";
    private DateTimeFormatter dateFormatter;

    @Autowired
    private ListingSearchDao listingSearchDao;

    @Autowired
    private BrokenSurveillanceRulesDAO brokenSurveillanceRulesDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private RuleComplianceCalculator ruleComplianceCalculator;

    @Autowired
    private JpaTransactionManager txManager;

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
            executorService = Executors.newFixedThreadPool(getThreadCountForJob());
            List<ListingSearchResult> listingsForReport = getListingsForReport();
            LOGGER.info(String.format("Found %s listings to process", listingsForReport.size()));

            List<BrokenSurveillanceRulesDTO> allBrokenRules = new ArrayList<BrokenSurveillanceRulesDTO>();

            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
            for (ListingSearchResult listing : listingsForReport) {
                futures.add(CompletableFuture.supplyAsync(() ->
                    processListing(listing.getId()), executorService)
                        .thenAccept(result -> allBrokenRules.addAll(result)));
            }

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");
            saveAllBrokenSurveillanceRules(allBrokenRules);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }

        LOGGER.info("********* Completed the Broken Surveillance Rules Creator job. *********");
    }

    private List<ListingSearchResult> getListingsForReport() {
        return listingSearchDao.getListingSearchResults().stream()
                .filter(listing -> isEdition2015(listing)
                        && (isCertificationStatusSuspendedByAcb(listing)
                                || hasSurveillances(listing)))
                .collect(Collectors.toList());
    }

    private boolean isEdition2015(ListingSearchResult listing) {
        return listing.getEdition() != null && listing.getEdition().getName().equals(EDITION_2015);
    }

    private boolean isCertificationStatusSuspendedByAcb(ListingSearchResult listing) {
        return listing.getCertificationStatus() != null
                && listing.getCertificationStatus().getName().equalsIgnoreCase(CertificationStatusType.SuspendedByAcb.getName());
    }

    private boolean hasSurveillances(ListingSearchResult listing) {
        return listing.getSurveillanceCount() > 0;
    }

    private List<BrokenSurveillanceRulesDTO> processListing(Long listingId) {
        CertifiedProductSearchDetails listing;
        try {
            listing = certifiedProductDetailsManager.getCertifiedProductDetailsUsingCache(listingId);
            LOGGER.info(String.format("Complete retrieving CertifiedProductDetails for: %s", listingId));
            return brokenRules(listing);
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not process listingId: %s  Reason: %s", listingId, e.getMessage()), e);
        }
        return null;
    }

    private List<BrokenSurveillanceRulesDTO> brokenRules(CertifiedProductSearchDetails listing)
            throws NumberFormatException, EntityRetrievalException {

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
                        if (isNonConformityOpen(nc)) {
                            boolean ncHasError = false;
                            BrokenSurveillanceRulesDTO rule = getDefaultBrokenRule(listing);
                            rule = addSurveillanceData(rule, surv);
                            rule = addNcData(rule, nc);

                            List<OversightRuleResult> oversightResult = ruleComplianceCalculator.calculateCompliance(listing, surv, nc);

                            for (OversightRuleResult currResult : oversightResult) {
                                String dateBrokenStr = "";
                                if (currResult.getDateBroken() != null) {
                                    dateBrokenStr = dateFormatter.format(currResult.getDateBroken());
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

    private Boolean isNonConformityOpen(SurveillanceNonconformity nonConformity) {
        return nonConformity.getNonconformityCloseDay() == null;
    }

    private BrokenSurveillanceRulesDTO getDefaultBrokenRule(CertifiedProductSearchDetails listing)
            throws NumberFormatException, EntityRetrievalException {

        BrokenSurveillanceRulesDTO base = new BrokenSurveillanceRulesDTO();
        base.setDeveloper(listing.getDeveloper().getName());
        base.setProduct(listing.getProduct().getName());
        base.setVersion(listing.getVersion().getVersion());
        base.setCertificationBody(
                getCertificationBody(
                        Long.parseLong(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString())));
        base.setChplProductNumber(listing.getChplProductNumber());
        String productDetailsUrl = env.getProperty("chplUrlBegin").trim() + env.getProperty("listingDetailsUrl") + listing.getId();
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
            base.setLengthySuspensionRule(dateFormatter.format(result.get(0).getDateBroken()));
        }
        return base;
    }

    private BrokenSurveillanceRulesDTO addSurveillanceData(BrokenSurveillanceRulesDTO rule, Surveillance surv) {

        if (surv.getFriendlyId() != null) {
            rule.setSurveillanceId(surv.getFriendlyId());
        }
        if (surv.getStartDay() != null) {
            rule.setDateSurveillanceBegan(dateFormatter.format(surv.getStartDay()));
        }
        if (surv.getEndDay() != null) {
            rule.setDateSurveillanceEnded(dateFormatter.format(surv.getEndDay()));
        }
        rule.setSurveillanceType(surv.getType().getName());
        rule.setNonconformity(false);
        return rule;
    }

    private BrokenSurveillanceRulesDTO addNcData(BrokenSurveillanceRulesDTO rule, SurveillanceNonconformity nc) {

        rule.setNonconformity(true);
        if (nc.getCriterion() != null) {
            rule.setNonconformityCriteria(CertificationCriterionService.formatCriteriaNumber(nc.getCriterion()));
        } else {
            rule.setNonconformityCriteria(nc.getNonconformityType());
        }
        if (nc.getNonconformityCloseDay() != null) {
            rule.setNonConformityCloseDate(nc.getNonconformityCloseDay());
        }
        if (nc.getDateOfDeterminationDay() != null) {
            rule.setDateOfDeterminationOfNonconformity(dateFormatter.format((nc.getDateOfDeterminationDay())));
        }
        if (nc.getCapApprovalDay() != null) {
            rule.setCorrectiveActionPlanApprovedDate(dateFormatter.format(nc.getCapApprovalDay()));
        }
        if (nc.getCapStartDay() != null) {
            rule.setDateCorrectiveActionBegan(dateFormatter.format(nc.getCapStartDay()));
        }
        if (nc.getCapMustCompleteDay() != null) {
            rule.setDateCorrectiveActionMustBeCompleted(dateFormatter.format(nc.getCapMustCompleteDay()));
        }
        if (nc.getCapEndDay() != null) {
            rule.setDateCorrectiveActionWasCompleted(dateFormatter.format(nc.getCapEndDay()));
        }

        if (nc.getCapApprovalDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getDateOfDeterminationDay(), nc.getCapApprovalDay());
            rule.setNumberOfDaysFromDeterminationToCapApproval(diffInDays);
        } else {
            long diffInDays = ChronoUnit.DAYS.between(nc.getDateOfDeterminationDay(), LocalDateTime.now());
            rule.setNumberOfDaysFromDeterminationToPresent(diffInDays);
        }

        if (nc.getCapApprovalDay() != null && nc.getCapStartDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getCapApprovalDay(), nc.getCapStartDay());
            rule.setNumberOfDaysFromCapApprovalToCapBegan(diffInDays);
        } else if (nc.getCapApprovalDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getCapApprovalDay(), LocalDateTime.now());
            rule.setNumberOfDaysFromCapApprovalToPresent(diffInDays);
        }

        if (nc.getCapStartDay() != null && nc.getCapEndDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getCapStartDay(), nc.getCapEndDay());
            rule.setNumberOfDaysFromCapBeganToCapCompleted(diffInDays);
        } else if (nc.getCapStartDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getCapStartDay(), LocalDateTime.now());
            rule.setNumberOfDaysFromCapBeganToPresent(diffInDays);
        }

        if (nc.getCapEndDay() != null && nc.getCapMustCompleteDay() != null) {
            long diffInDays = ChronoUnit.DAYS.between(nc.getCapMustCompleteDay(), nc.getCapEndDay());
            rule.setDifferenceFromCapCompletedAndCapMustBeCompleted(diffInDays);
        }
        return rule;
    }

    private void saveAllBrokenSurveillanceRules(List<BrokenSurveillanceRulesDTO> allBrokenRules) {

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    LOGGER.info("Deleting {} OBE rules", brokenSurveillanceRulesDAO.findAll().size());
                    brokenSurveillanceRulesDAO.deleteAll();
                    LOGGER.info("Creating {} current broken rules", allBrokenRules.size());
                    brokenSurveillanceRulesDAO.create(allBrokenRules);
                } catch (Exception e) {
                    status.setRollbackOnly();
                }
            }
        });
    }

    private CertificationBodyDTO getCertificationBody(long certificationBodyId) throws EntityRetrievalException {
        return certificationBodyDAO.getById(certificationBodyId);
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
