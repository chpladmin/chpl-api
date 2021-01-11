package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListingValidationCreatorJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private ListingValidatorFactory validatorFactory;

    @Autowired
    private ListingValidationReportDAO listingValidationReportDAO;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cache Status Age job. *********");
        try {
            List<CertifiedProductSearchDetails> listingsWithErrors = getAll2015CertifiedProducts().parallelStream()
                    .map(listing -> getCertifiedProductSearchDetails(listing.getId()))
                    .map(detail -> validateListing(detail))
                    .filter(detail -> doValidationErrorsExist(detail))
                    .collect(Collectors.toList());

            for (CertifiedProductSearchDetails listing : listingsWithErrors) {
                createListingValidationReport(listing);
            }
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Cache Status Age job. *********");
    }

    private List<CertifiedProductDetailsDTO> getAll2015CertifiedProducts() {
        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");
        return listings;
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long certifiedProductId) {

        try {
            LOGGER.info("Retrieving details for listing: " + certifiedProductId);
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(certifiedProductId);
            LOGGER.info("Completed details for listing: " + certifiedProductId);
            return listing;
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return null;
        }
    }

    private CertifiedProductSearchDetails validateListing(CertifiedProductSearchDetails listing) {
        LOGGER.info("Starting validation of listing: " + listing.getId());
        Validator validator = validatorFactory.getValidator(listing);
        validator.validate(listing);
        LOGGER.info("Completed validation of listing: " + listing.getId());
        return listing;
    }

    private boolean doValidationErrorsExist(CertifiedProductSearchDetails listing) {
        LOGGER.info("Starting check of errors of listing: " + listing.getId());
        boolean errorsExist = listing.getErrorMessages().size() > 0;
        LOGGER.info("Completed check of errors of listing: " + listing.getId());
        return errorsExist;
    }

    private List<ListingValidationReport> createListingValidationReport(CertifiedProductSearchDetails listing) {
     // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return txTemplate.execute(status -> {
                try {
                    LOGGER.info("Starting save of report data for: " + listing.getId());
                    List<ListingValidationReport> reports = listing.getErrorMessages().stream()
                            .map(error -> listingValidationReportDAO.create(ListingValidationReport.builder()
                                .chplProductNumber(listing.getChplProductNumber())
                                .productName(listing.getProduct().getName())
                                .certificationStatusName(listing.getCurrentStatus().getStatus().getName())
                                .errorMessage(error)
                                .reportDate(new Date())
                                .lastModifiedUser(User.SYSTEM_USER_ID)
                                .build()))
                            .collect(Collectors.toList());
                    LOGGER.info("Completed save of report data for: " + listing.getId());
                    return reports;
                } catch (Exception e) {
                    LOGGER.error("Error inserting listing validation errors. Rolling back transaction.", e);
                    status.setRollbackOnly();
                    return new ArrayList<ListingValidationReport>();
                }
        });


    }
}
