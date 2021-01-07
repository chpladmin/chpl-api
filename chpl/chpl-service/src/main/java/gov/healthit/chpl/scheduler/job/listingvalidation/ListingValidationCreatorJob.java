package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.util.List;
import java.util.stream.Collectors;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

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
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private ListingValidatorFactory validatorFactory;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cache Status Age job. *********");
        try {
            List<CertifiedProductSearchDetails> listingsWithErrors = getAll2015CertifiedProducts().parallelStream()
                    .map(listing -> getCertifiedProductSearchDetails(listing.getId()))
                    .map(detail -> validateListing(detail))
                    .filter(detail -> detail.getErrorMessages().size() > 0)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Cache Status Age job. *********");
    }

    private List<CertifiedProductDetailsDTO> getAll2015CertifiedProducts() {
        return certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.toString());
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long certifiedProductId) {
        try {
            return certifiedProductDetailsManager.getCertifiedProductDetails(certifiedProductId);
        } catch (EntityRetrievalException e) {
            LOGGER.catching(e);
            return null;
        }
    }

    private CertifiedProductSearchDetails validateListing(CertifiedProductSearchDetails listing) {
        Validator validator = validatorFactory.getValidator(listing);
        validator.validate(listing);
        return listing;
    }
}
