package gov.healthit.chpl.scheduler.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import gov.healthit.chpl.scheduler.DataCollectorAsyncSchedulerHelper;
import gov.healthit.chpl.util.ChplProductNumberUtil;

/**
 * Initiates and runs the the Quartz job that generates the data that is used to to create
 * the Inheritance Errors Report notification.
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
    private DataCollectorAsyncSchedulerHelper dataCollectorAsyncSchedulerHelper;

    @Autowired
    private ListingGraphDAO listingGraphDAO;

    @Autowired
    private MessageSource messageSource;

    /**
     * Constructor to initialize InheritanceErrorsReportCreatorJob object.
     * @throws Exception is thrown
     */
    public InheritanceErrorsReportCreatorJob() throws Exception {
        super();
        loadProperties();
    }

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        dataCollectorAsyncSchedulerHelper.setLogger(LOGGER);

        LOGGER.info("********* Starting the Inheritance Error Report Creator job. *********");
        List<CertifiedProductSearchDetails> results = retrieveData();
        List<InheritanceErrorsReportDTO> errors = new ArrayList<InheritanceErrorsReportDTO>();
        for (CertifiedProductSearchDetails listing : results) {
            String reason = breaksIcsRules(listing);
            if (!StringUtils.isEmpty(reason)) {
                InheritanceErrorsReportDTO item = new InheritanceErrorsReportDTO();
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
                errors.add(item);
            }
        }
        inheritanceErrorsReportDAO.deleteAll();
        if (errors.size() > 0) {
            saveInheritanceErrorsReport(errors);
        }
        LOGGER.info("Completed the Inheritance Error Report Creator job. *********");
    }

    private void saveInheritanceErrorsReport(final List<InheritanceErrorsReportDTO> items) {
        try {
            inheritanceErrorsReportDAO.create(items);
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Unable to save Inheritance Errors Report {} with error message {}",
                    items.toString(), e.getLocalizedMessage());
        }
    }

    private Properties loadProperties() throws IOException {
        InputStream in =
                InheritanceErrorsReportCreatorJob.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
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
        LOGGER.info("2015 Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(
                certifiedProducts);

        return certifiedProductsWithDetails;
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
        LOGGER.info("Time to retrieve details: " + (endTime.getTime() - startTime.getTime()));

        return details;
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
        } catch (Exception ex) {
            LOGGER.error("Could not compare ICS value " + icsCodePart + " to inherits boolean value", ex);
        }
        return null;
    }
}
