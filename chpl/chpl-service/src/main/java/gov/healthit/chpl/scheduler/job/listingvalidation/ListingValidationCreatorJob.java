package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "listingValidationReportCreatorJobLogger")
public class ListingValidationCreatorJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private ListingDetailsNormalizer listingNormalizer;

    @Autowired
    private ListingValidatorFactory validatorFactory;

    @Autowired
    private ListingValidationReportDAO listingValidationReportDAO;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    @Value("${listingValidation.report.bannedDeveloperMessageRegex}")
    private String bannedDeveloperMessageRegex;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Listing Validation Creator job. *********");
        try {
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
                        // This will control how many threads are used by the parallelStream.  By default parallelStream
                        // will use the # of processors - 1 threads.  We want to be able to limit this.
                        ForkJoinPool pool = new ForkJoinPool(threadCount);
                        List<CertifiedProductSearchDetails> listingsWithErrors = pool.submit(() -> getListingsWithErrors()).get();

                        deleteAllExistingListingValidationReports();

                        listingsWithErrors.stream()
                                .forEach(listing -> createListingValidationReport(listing));
                    } catch (Exception e) {
                        LOGGER.error("Error inserting listing validation errors. Rolling back transaction.", e);
                        status.setRollbackOnly();
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Listing Validation Creator job. *********");
    }

    private List<CertifiedProductSearchDetails> getListingsWithErrors() {
        return getAll2015CertifiedProducts().parallelStream()
                .map(listing -> getCertifiedProductSearchDetails(listing.getId()))
                .map(detail -> validateListing(detail))
                .filter(detail -> doValidationErrorsExist(detail))
                .collect(Collectors.toList());
    }

    private List<CertifiedProductDetailsDTO> getAll2015CertifiedProducts() {
        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");
        return listings.stream()
                .collect(Collectors.toList());
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long certifiedProductId) {
        try {
            long start = (new Date()).getTime();
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(certifiedProductId);
            LOGGER.info("Completed details for listing(" + ((new Date()).getTime() - start) + "ms): " + certifiedProductId);
            return listing;
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve details for listing id: " + certifiedProductId);
            LOGGER.catching(e);
            return null;
        }
    }

    private CertifiedProductSearchDetails validateListing(CertifiedProductSearchDetails listing) {
        try {
            listingNormalizer.normalize(listing);
            Validator validator = validatorFactory.getValidator(listing);
            validator.validate(listing);
            LOGGER.info("Completed validation of listing: " + listing.getId());
            return listing;
        } catch (Exception e) {
            LOGGER.catching(e);
            return listing;
        }
    }

    private void deleteAllExistingListingValidationReports() {
        LOGGER.info("Started deletion of all existing listing validation reports");
        listingValidationReportDAO.deleteAll();
        LOGGER.info("Completed deletion of all existing listing validation reports");
    }

    private boolean doValidationErrorsExist(CertifiedProductSearchDetails listing) {
        boolean errorsExist = listing.getErrorMessages().size() > 0;
        return errorsExist;
    }

    private List<ListingValidationReport> createListingValidationReport(CertifiedProductSearchDetails listing) {
        List<ListingValidationReport> reports = listing.getErrorMessages().stream()
                .filter(error -> !isBannedDeveloperErrorMessage(error))
                .map(error -> listingValidationReportDAO.create(ListingValidationReport.builder()
                    .certifiedProductId(listing.getId())
                    .chplProductNumber(listing.getChplProductNumber())
                    .certificationBodyId(Long.parseLong(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()))
                    .product(listing.getProduct().getName())
                    .version(listing.getVersion().getVersion())
                    .developer(listing.getDeveloper().getName())
                    .certificationBody(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString())
                    .certificationStatusName(listing.getCurrentStatus().getStatus().getName())
                    .errorMessage(error)
                    .reportDate(ZonedDateTime.now(Clock.systemUTC()))
                    .lastModifiedUser(User.SYSTEM_USER_ID)
                    .build()))
                .collect(Collectors.toList());
        LOGGER.info("Completed save of report data for: " + listing.getId());
        return reports;
    }

    private boolean isBannedDeveloperErrorMessage(String message) {
        try {
            Pattern pattern = Pattern.compile(bannedDeveloperMessageRegex);
            Matcher matcher = pattern.matcher(message);
            return matcher.find();
        } catch (Exception e) {
            LOGGER.error("Message being test when error occurred: " + message);
            LOGGER.catching(e);
            return false;
        }
    }
}
