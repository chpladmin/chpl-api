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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dao.scheduler.InheritanceErrorsReportDAO;
import gov.healthit.chpl.dao.search.CertifiedProductSearchDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.DataCollectorAsyncSchedulerHelper;
import gov.healthit.chpl.scheduler.JobConfig;

/**
 * Initiates and runs the the Quartz job that generates the data that is used to to create
 * the Inheritance Errors Report notification.
 * @author alarned
 *
 */
@DisallowConcurrentExecution
public class InheritanceErrorsReportCreatorJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger(InheritanceErrorsReportCreatorJob.class);
    private static final String EDITION_2015 = "2015";
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private static final int MIN_NUMBER_TO_NOT_NEED_PREFIX = 10;
    private CertifiedProductSearchDAO certifiedProductSearchDAO;
    private InheritanceErrorsReportDAO inheritanceErrorsReportDAO;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private DataCollectorAsyncSchedulerHelper dataCollectorAsyncSchedulerHelper;
    private ListingGraphDAO listingGraphDAO;
    private Properties props;
    private AbstractApplicationContext context;
    private MessageSource messageSource;

    /**
     * Constructor to initialize InheritanceErrorsReportCreatorJob object.
     * @throws Exception is thrown
     */
    public InheritanceErrorsReportCreatorJob() throws Exception {
        super();
        setLocalContext();
        context = new AnnotationConfigApplicationContext(JobConfig.class);
        initiateSpringBeans(context);
        loadProperties();
    }

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        setCertifiedProductDetailsManager((CertifiedProductDetailsManager)
                context.getBean("certifiedProductDetailsManager"));
        setCertifiedProductSearchDAO((CertifiedProductSearchDAO) context.getBean("certifiedProductSearchDAO"));
        setDataCollectorAsyncSchedulerHelper((DataCollectorAsyncSchedulerHelper)
                context.getBean("dataCollectorAsyncSchedulerHelper"));
        setInheritanceErrorsReportDAO((InheritanceErrorsReportDAO) context.getBean("inheritanceErrorsReportDAO"));
        setListingGraphDAO((ListingGraphDAO) context.getBean("listingGraphDao"));
        setMessageSource((MessageSource) context.getBean("messageSource"));
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
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
                item.setAcb(listing.getCertifyingBody().get("name").toString());
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
    }

    private void saveInheritanceErrorsReport(final List<InheritanceErrorsReportDTO> items) {
        try {
            inheritanceErrorsReportDAO.create(items);
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Unable to save Inheritance Errors Report {} with error message {}",
                    items.toString(), e.getLocalizedMessage());
        } finally {
            context.close();
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

    private void setCertifiedProductDetailsManager(
            final CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
    }

    private void setCertifiedProductSearchDAO(final CertifiedProductSearchDAO certifiedProductSearchDAO) {
        this.certifiedProductSearchDAO = certifiedProductSearchDAO;
    }

    private void setDataCollectorAsyncSchedulerHelper(
            final DataCollectorAsyncSchedulerHelper dataCollectorAsyncSchedulerHelper) {
        this.dataCollectorAsyncSchedulerHelper = dataCollectorAsyncSchedulerHelper;
    }

    private void setInheritanceErrorsReportDAO(final InheritanceErrorsReportDAO inheritanceErrorsReportDAO) {
        this.inheritanceErrorsReportDAO = inheritanceErrorsReportDAO;
    }

    private void setListingGraphDAO(final ListingGraphDAO listingGraphDAO) {
        this.listingGraphDAO = listingGraphDAO;
    }

    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
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
        if (uniqueIdParts == null || uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            return null;
        }
        String icsCodePart = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
        try {
            Integer icsCode = new Integer(icsCodePart);
            boolean hasIcs = icsCode.intValue() == 1
                    || (listing.getIcs() != null && listing.getIcs().getInherits() == Boolean.TRUE);

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
                if (largestIcs != null && icsCode.intValue() != expectedIcsCode) {
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
