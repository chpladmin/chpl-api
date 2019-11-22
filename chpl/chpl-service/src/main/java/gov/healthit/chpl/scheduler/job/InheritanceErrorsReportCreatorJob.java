package gov.healthit.chpl.scheduler.job;

import java.text.ParseException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
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
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    private Environment env;

    private Date curesRuleEffectiveDate;

    /**
     * Constructor to initialize InheritanceErrorsReportCreatorJob object.
     * 
     * @throws Exception
     *             is thrown
     */
    public InheritanceErrorsReportCreatorJob() throws Exception {
        super();
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Inheritance Error Report Creator job. *********");

        curesRuleEffectiveDate = getCuresRuleEffectiveDate();
        if (curesRuleEffectiveDate == null) {
            return;
        }

        inheritanceErrorsReportDAO.deleteAll();
        List<CertifiedProductFlatSearchResult> listings = certifiedProductSearchDAO.getAllCertifiedProducts();
        List<CertifiedProductFlatSearchResult> certifiedProducts = filterData(listings);

        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);

            for (CertifiedProductFlatSearchResult result : certifiedProducts) {
                CompletableFuture.supplyAsync(() -> getCertifiedProductSearchDetails(result.getId()), executorService)
                        .thenApply(cp -> check(cp))
                        .thenAccept(error -> saveInheritanceErrorsReportSingle(error));
            }
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
                item.setAcb(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
                String productDetailsUrl = env.getProperty("chplUrlBegin").trim();
                LOGGER.info("productDetailsUrl = " + productDetailsUrl);
                if (!productDetailsUrl.endsWith("/")) {
                    productDetailsUrl += "/";
                }
                productDetailsUrl += "#/product/" + listing.getId();
                item.setUrl(productDetailsUrl);
                item.setReason(reason);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Completed check of listing [" + listing.getChplProductNumber() + "]");
        return item;
    }

    private void saveInheritanceErrorsReportSingle(final InheritanceErrorsReportDTO item) {
        if (item == null) {
            return;
        }
        try {
            inheritanceErrorsReportDAO.create(item);
            LOGGER.info("Completed saving of error [" + item.getChplProductNumber() + "]");
        } catch (Exception e) {
            LOGGER.error("Unable to save Inheritance Errors Report {} with error message {}",
                    item.toString(), e.getLocalizedMessage());
        }
    }

    private List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(cp -> cp.getEdition().equalsIgnoreCase(EDITION_2015))
                .collect(Collectors.toList());
    }

    private String breaksIcsRules(final CertifiedProductSearchDetails listing) {
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
        String dateFromPropertiesFile = env.getProperty("cures.ruleEffectiveDate");
        LOGGER.info("cures.ruleEffectiveDate = " + dateFromPropertiesFile);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return sdf.parse(dateFromPropertiesFile);
        } catch (ParseException e) {
            LOGGER.error("Could not parse: " + dateFromPropertiesFile, e);
            return null;
        }
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
