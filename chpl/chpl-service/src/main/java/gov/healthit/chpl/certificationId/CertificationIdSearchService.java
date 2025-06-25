package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationIdSearchService {
    private CertificationIdManager certificationIdManager;
    private CertifiedProductManager certifiedProductManager;
    private CertificationIdYearCalculator certIdYearCalculator;
    private ValidatorFactory validatorFactory;

    @Autowired
    public CertificationIdSearchService(CertificationIdManager certificationIdManager,
            CertifiedProductManager certifiedProductManager,
            CertificationIdYearCalculator certIdYearCalculator,
            ValidatorFactory validatorFactory) {
        this.certificationIdManager = certificationIdManager;
        this.certifiedProductManager = certifiedProductManager;
        this.certIdYearCalculator = certIdYearCalculator;
        this.validatorFactory = validatorFactory;
    }

    @Transactional
    public CertificationIdLookupResults findCertificationIdByCertificationId(String certificationId,
            Boolean includeCriteria,
            Boolean includeCqms) throws InvalidArgumentsException, EntityRetrievalException, CertificationIdException {
        CertificationIdLookupResults results = new CertificationIdLookupResults();
        try {
            // Lookup the Cert ID
            CertificationIdDTO certId = certificationIdManager.getByCertificationId(certificationId);
            if (certId != null) {
                results.setEhrCertificationId(certId.getCertificationId());
                results.setYear(certId.getYear());

                // Find the listings associated with the Cert ID
                List<Long> listingIds = certificationIdManager.getListingIdsByCertificationId(certId.getId());
                List<CertifiedProductDetailsDTO> listingDtos = certifiedProductManager.getDetailsByIds(listingIds);
                // Add product data to results
                results.setProducts(listingDtos.stream()
                        .map(listing -> new CertificationIdLookupResults.Product(listing))
                        .collect(Collectors.toList()));

                // Add criteria and cqms met to results
                if (includeCriteria || includeCqms) {
                    Validator validator = this.validatorFactory.getValidator(certId.getYear());

                    // Lookup Criteria for Validating
                    List<CertificationCriterion> criteria = certificationIdManager.getCriteriaMetByCertifiedProductIds(listingIds);

                    // Lookup CQMs for Validating
                    List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(listingIds);

                    boolean isValid = validator.validate(criteria, cqmDtos);
                    if (isValid) {
                        if (includeCriteria) {
                            results.setCriteria(validator.getCriteriaMet().keySet());
                        }
                        if (includeCqms) {
                            results.setCqms(validator.getCqmsMet().keySet());
                        }
                    }
                }
            } else {
                LOGGER.error("Certification ID " + certificationId + " does not exist.");
            }
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to lookup Certification ID " + certificationId, ex);
            throw new EntityRetrievalException("Unable to lookup Certification ID " + certificationId + ".");
        }

        return results;
    }

    public CertificationIdResults findCertificationByProductIds(List<Long> listingIds, Boolean create)
            throws InvalidArgumentsException, CertificationIdException {
        if (CollectionUtils.isEmpty(listingIds)) {
            return null;
        }

        List<CertifiedProductDetailsDTO> listingDtos = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listingDtos = certifiedProductManager.getDetailsByIds(listingIds);
        } catch (EntityRetrievalException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        if (create) {
            Optional<CertifiedProductDetailsDTO> invalidListing = listingDtos.stream()
                .filter(listing -> !isEditionlessOrCuresUpdate(listing))
                .findAny();
            if (invalidListing.isPresent()) {
                throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Cures Update Listings");
            }
        }

        // Add products to results
        CertificationIdResults results = new CertificationIdResults();
        results.setProducts(listingDtos.stream()
                .map(listing -> new CertificationIdResults.Product(listing))
                .collect(Collectors.toList()));
        //get the "year" for this cms id
        results.setYear(certIdYearCalculator.getCurrentCertIdYear());

        // Validate the collection
        Validator validator = this.validatorFactory.getValidator(results.getYear());

        // Lookup Criteria for Validating
        List<CertificationCriterion> criteria = certificationIdManager.getCriteriaMetByCertifiedProductIds(listingIds);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(listingIds);

        boolean isValid = validator.validate(criteria, cqmDtos);
        results.setValid(isValid);
        results.setMetPercentages(validator.getPercents());
        results.setMetCounts(validator.getCounts());
        results.setMissingCombo(validator.getMissingCombo());
        results.setMissingOr(validator.getMissingOr());
        results.setMissingAnd(validator.getMissingAnd());
        results.setMissingXOr(validator.getMissingXOr());

        // Lookup CERT ID
        if (validator.isValid()) {
            CertificationIdDTO existingCertId = null;
            try {
                existingCertId = certificationIdManager.getByListings(listingDtos, results.getYear());
                if (existingCertId != null) {
                    results.setEhrCertificationId(existingCertId.getCertificationId());
                } else {
                    if ((create) && (results.isValid())) {
                        // Generate a new ID
                        existingCertId = certificationIdManager.create(listingIds, results.getYear());
                        results.setEhrCertificationId(existingCertId.getCertificationId());
                    }
                }
            } catch (EntityRetrievalException | EntityCreationException | ActivityException ex) {
                LOGGER.error("Unable to look up cert id by listings", ex);
                throw new CertificationIdException("Unable to retrieve a Certification ID.");
            }
        }
        return results;
    }

    private boolean isEditionlessOrCuresUpdate(CertifiedProductDetailsDTO listing) {
        if (StringUtils.isEmpty(listing.getYear()) && listing.getCuresUpdate() == null) {
            return true;
        }
        return listing.getYear().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                && BooleanUtils.isTrue(listing.getCuresUpdate());
    }
}
