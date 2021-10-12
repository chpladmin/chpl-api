package gov.healthit.chpl.scheduler.job;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class CertifiedProduct2015Gatherer {
    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    public List<CertifiedProductSearchDetails> getAll2015CertifiedProducts(Logger logger) {
        logger.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        logger.info("Completed retreiving all 2015 listings");
        return listings.parallelStream()
                .map(dto -> getCertifiedProductSearchDetails(dto.getId(), logger))
                .collect(Collectors.toList());
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long certifiedProductId, Logger logger) {
        try {
            long start = (new Date()).getTime();
            CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetailsUsingCache(certifiedProductId);
            logger.info("Completed details for listing(" + ((new Date()).getTime() - start) + "ms): " + certifiedProductId);
            return listing;
        } catch (EntityRetrievalException e) {
            logger.error("Could not retrieve details for listing id: " + certifiedProductId);
            logger.catching(e);
            return null;
        }
    }
}
