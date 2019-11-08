package gov.healthit.chpl.scheduler.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;

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
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private static final int MIN_NUMBER_TO_NOT_NEED_PREFIX = 10;
    private Properties props;

    @Autowired
    private CertifiedProductSearchDAO certifiedProductSearchDAO;

    @Autowired
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private ListingGraphDAO listingGraphDAO;

    @Autowired
    private MessageSource messageSource;

    /**
     * Constructor to initialize InheritanceErrorsReportCreatorJob object.
     * 
     * @throws Exception
     *             is thrown
     */
    public InheritanceErrorsReportCreatorJob() throws Exception {
        super();
        loadProperties();
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Inheritance Error Report Creator job. *********");

        inheritanceErrorsReportDAO.deleteAll();
        List<CertifiedProductFlatSearchResult> listings = certifiedProductSearchDAO.getAllCertifiedProducts();
        List<CertifiedProductFlatSearchResult> certifiedProducts = filterData(listings);
        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(8);

            for (CertifiedProductFlatSearchResult result : certifiedProducts) {
                CompletableFuture<Void> future = CompletableFuture
                        .supplyAsync(() -> getCertifiedProductSearchDetails(result.getId()), executorService)
                        .thenApply(cp -> check(cp))
                        .thenAccept(error -> saveInheritanceErrorsReportSingle(error));
            }
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
        String reason = breaksIcsRules(listing);
        if (!StringUtils.isEmpty(reason)) {
            item = new InheritanceErrorsReportDTO();
            item.setChplProductNumber(listing.getChplProductNumber());
            item.setDeveloper(listing.getDeveloper().getName());
            item.setProduct(listing.getProduct().getName());
            item.setVersion(listing.getVersion().getVersion());
            item.setAcb(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString());
            String productDetailsUrl = props.getProperty("chplUrlBegin").trim();
            if (!productDetailsUrl.endsWith("/")) {
                productDetailsUrl += "/";
            }
            productDetailsUrl += "#/product/" + listing.getId();
            item.setUrl(productDetailsUrl);
            item.setReason(reason);
        }
        LOGGER.info("Completed check of listing [" + listing.getChplProductNumber() + "]");
        return item;
    }

    private void saveInheritanceErrorsReportSingle(final InheritanceErrorsReportDTO item) {
        try {
            inheritanceErrorsReportDAO.create(item);
            LOGGER.info("Completed saving of error [" + item.getChplProductNumber() + "]");
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Unable to save Inheritance Errors Report {} with error message {}",
                    item.toString(), e.getLocalizedMessage());
        }
    }

    private Properties loadProperties() throws IOException {
        InputStream in = InheritanceErrorsReportCreatorJob.class.getClassLoader()
                .getResourceAsStream(DEFAULT_PROPERTIES_FILE);
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

    private List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (result.getEdition().equalsIgnoreCase(EDITION_2015)) {
                results.add(result);
            }
        }
        return results;
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
                return messageSource.getMessage(new DefaultMessageSourceResolvable("ics.noInheritanceError"),
                        LocaleContextHolder.getLocale());
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
                    return String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("ics.badIncrementError"),
                                    LocaleContextHolder.getLocale()),
                            existing, expected);
                }
            }

            if (listing.getCertificationEdition().get("id").equals("3") && !hasIcs) {
                if (listing.getCertificationResults().stream()
                        .filter(cert -> cert.isGap())
                        .count() > 0) {

                    return "Found the new error!";
                }

            }

        } catch (Exception ex) {
            LOGGER.error("Could not compare ICS value " + icsCodePart + " to inherits boolean value", ex);
        }
        return null;
    }
}
