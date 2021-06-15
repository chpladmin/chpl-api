package gov.healthit.chpl.scheduler.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Initiates and runs the the Quartz job that generates the data that is used to to create the Inheritance Errors Report
 * notification.
 *
 * @author alarned
 *
 */
@DisallowConcurrentExecution
public class InheritanceErrorsReportCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("inheritanceErrorsReportCreatorJobLogger");
    private static final String EDITION_2015 = "2015";
    private static final int MIN_NUMBER_TO_NOT_NEED_PREFIX = 10;

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Autowired
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private ListingGraphDAO listingGraphDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    @Autowired
    private SpecialProperties specialProperties;

    private Date curesRuleEffectiveDate;

    public InheritanceErrorsReportCreatorJob() throws Exception {
        super();
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Inheritance Error Report Creator job. *********");

        curesRuleEffectiveDate = getCuresRuleEffectiveDate();
        if (curesRuleEffectiveDate == null) {
            return;
        }

        List<CertifiedProductFlatSearchResult> listings = certifiedProductSearchDAO.getFlatCertifiedProducts();
        List<CertifiedProductFlatSearchResult> certifiedProducts = filterData(listings);

        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            List<InheritanceErrorsReportDTO> allInheritanceErrors = new ArrayList<InheritanceErrorsReportDTO>();

            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
            for (CertifiedProductFlatSearchResult result : certifiedProducts) {
                futures.add(CompletableFuture.supplyAsync(() -> getCertifiedProductSearchDetails(result.getId()), executorService)
                        .thenApply(cp -> check(cp))
                        .thenAccept(error -> {
                            if (error != null) {
                                allInheritanceErrors.add(error);
                            }
                        }));
            }

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");
            saveAllInheritanceErrors(allInheritanceErrors);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
        LOGGER.info("Completed the Inheritance Error Report Creator job. *********");
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long id) {
        CertifiedProductSearchDetails cp = null;
        try {
            cp = certifiedProductDetailsManager.getCertifiedProductDetails(id);
            LOGGER.info("Completed retrieval of listing [" + cp.getChplProductNumber() + "]");
        } catch (Exception e) {
            LOGGER.error("Could not retrieve listing [" + id + "] - " + e.getMessage(), e);
        }
        return cp;
    }

    private InheritanceErrorsReportDTO check(CertifiedProductSearchDetails listing) {
        if (listing == null) {
            return null;
        }
        InheritanceErrorsReportDTO item = null;
        try {
            String reason = breaksIcsRules(listing);
            if (!StringUtils.isEmpty(reason)) {
                item = new InheritanceErrorsReportDTO();
                item.setChplProductNumber(listing.getChplProductNumber());
                item.setDeveloper(listing.getDeveloper().getName());
                item.setProduct(listing.getProduct().getName());
                item.setVersion(listing.getVersion().getVersion());
                item.setCertificationBody(
                        getCertificationBody(
                                Long.parseLong(
                                        listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString())));

                String productDetailsUrl = env.getProperty("chplUrlBegin").trim() + env.getProperty("listingDetailsUrl") + listing.getId();
                LOGGER.info("productDetailsUrl = " + productDetailsUrl);
                item.setUrl(productDetailsUrl);
                item.setReason(reason);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Completed check of listing [" + listing.getChplProductNumber() + "]");
        return item;
    }

    private void saveAllInheritanceErrors(List<InheritanceErrorsReportDTO> allInheritanceErrors) {

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
                    LOGGER.info("Deleting {} OBE inheritance errors", inheritanceErrorsReportDAO.findAll().size());
                    inheritanceErrorsReportDAO.deleteAll();
                    LOGGER.info("Creating {} current inheritance errors", allInheritanceErrors.size());
                    inheritanceErrorsReportDAO.create(allInheritanceErrors);
                } catch (Exception e) {
                    LOGGER.error("Error updating to latest inheritance errors. Rolling back transaction.", e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private List<CertifiedProductFlatSearchResult> filterData(List<CertifiedProductFlatSearchResult> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(cp -> cp.getEdition().equalsIgnoreCase(EDITION_2015))
                .collect(Collectors.toList());
    }

    private String breaksIcsRules(CertifiedProductSearchDetails listing) {
        String uniqueId = listing.getChplProductNumber();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return null;
        }
        String icsCodePart = uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX];
        try {
            Integer icsCode = Integer.valueOf(icsCodePart);
            boolean hasIcs = icsCode.intValue() == 1
                    || (listing.getIcs() != null && listing.getIcs().getInherits().booleanValue());

            // check if listing has ICS but no family ties
            if (hasIcs && (listing.getIcs() == null || listing.getIcs().getParents() == null
                    || listing.getIcs().getParents().size() == 0)) {
                return errorMessageUtil.getMessage("ics.noInheritanceError");
            }

            // check if this listing has correct ICS increment
            // this listing's ICS code must be greater than the max of parent
            // ICS codes
            if (hasIcs && listing.getIcs() != null && listing.getIcs().getParents() != null
                    && listing.getIcs().getParents().size() > 0) {
                List<Long> parentIds = new ArrayList<Long>();
                for (CertifiedProduct potentialParent : listing.getIcs().getParents()) {
                    parentIds.add(potentialParent.getId());
                }

                Integer largestIcs = listingGraphDAO.getLargestIcs(parentIds);
                int expectedIcsCode = largestIcs.intValue() + 1;
                if (icsCode.intValue() != expectedIcsCode) {
                    String existing = (icsCode.toString().length() == 1 ? "0" : "") + icsCode.toString();
                    String expected = (expectedIcsCode < MIN_NUMBER_TO_NOT_NEED_PREFIX ? "0" : "") + expectedIcsCode;
                    return errorMessageUtil.getMessage("ics.badIncrementError", existing, expected);
                }
            }

            if (!hasIcs && doesGapExistForListing(listing) && isCertificationDateAfterRuleEffectiveDate(listing)) {
                return errorMessageUtil.getMessage("ics.gapListingError");
            }

        } catch (Exception ex) {
            LOGGER.error("Could not compare ICS value " + icsCodePart + " to inherits boolean value", ex);
        }
        return null;
    }

    private boolean doesGapExistForListing(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(cert -> cert.isGap() != null ? cert.isGap() : false)
                .count() > 0;
    }

    private boolean isCertificationDateAfterRuleEffectiveDate(CertifiedProductSearchDetails listing) {
        Date certDate = new Date(listing.getCertificationDate());
        return certDate.equals(curesRuleEffectiveDate) || certDate.after(curesRuleEffectiveDate);
    }

    private Date getCuresRuleEffectiveDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date effectiveRuleDate = specialProperties.getEffectiveRuleDate();
        LOGGER.info("cures.ruleEffectiveDate = " + sdf.format(effectiveRuleDate));
        return effectiveRuleDate;
    }

    private CertificationBodyDTO getCertificationBody(long certificationBodyId) throws EntityRetrievalException {
        return certificationBodyDAO.getById(certificationBodyId);
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
