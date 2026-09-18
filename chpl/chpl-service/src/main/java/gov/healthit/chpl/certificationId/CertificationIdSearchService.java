package gov.healthit.chpl.certificationId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CertificationIdSearchService {
    private CertificationIdManager certificationIdManager;
    private CertifiedProductDetailsManager cpdManager;
    private CertificationIdYearCalculator certIdYearCalculator;
    private ValidatorFactory validatorFactory;

    @Autowired
    public CertificationIdSearchService(CertificationIdManager certificationIdManager,
            CertifiedProductDetailsManager cpdManager,
            CertificationIdYearCalculator certIdYearCalculator,
            ValidatorFactory validatorFactory) {
        this.certificationIdManager = certificationIdManager;
        this.cpdManager = cpdManager;
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
                List<CertifiedProductDetailsForCertificationId> listings = getAllListingDetails(listingIds);
                // Add product data to results
                results.setProducts(listings.stream()
                        .map(listing -> new CertificationIdLookupResults.Product(listing))
                        .collect(Collectors.toList()));

                // Add criteria and cqms met to results
                if (includeCriteria || includeCqms) {
                    Validator validator = this.validatorFactory.getValidator(certId.getYear());
                    validator.getListings().addAll(listings);
                    boolean isValid = validator.validate();
                    if (isValid) {
                        if (includeCriteria) {
                            results.setCriteria(validator.getCriteriaMet());
                        }
                        if (includeCqms) {
                            results.setCqms(validator.getCqmsMet());
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

    public CertificationIdResults createCertificationId(List<Long> listingIds, String certificationYear)
            throws InvalidArgumentsException, EntityRetrievalException, CertificationIdException {
        List<CertificationIdResults> certIdResults = findCertificationByListingIds(listingIds, Stream.of(certificationYear).toList(), true);
        if (!CollectionUtils.isEmpty(certIdResults)) {
            return certIdResults.get(0);
        }
        return null;
    }

    public List<CertificationIdResults> findCertificationByListingIds(List<Long> listingIds, List<String> certificationYears, Boolean create)
            throws InvalidArgumentsException, EntityRetrievalException, CertificationIdException {
        if (CollectionUtils.isEmpty(listingIds)) {
            throw new InvalidArgumentsException("At least one listing ID is required.");
        } else if (CollectionUtils.isEmpty(certificationYears)) {
            throw new InvalidArgumentsException("At least one certificaiton year is required.");
        }

        List<CertifiedProductDetailsForCertificationId> listings = getAllListingDetails(listingIds);
        if (create) {
            Optional<CertifiedProductDetailsForCertificationId> invalidListing = listings.stream()
                .filter(listing -> !isEditionlessOrCuresUpdate(listing))
                .findAny();
            if (invalidListing.isPresent()) {
                throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Cures Update Listings");
            }
        }

        List<CertificationIdResults> resultsForAllYears = certificationYears.stream()
            .map(certYear -> {
                try {
                    return findCertificationIdByListings(listings, certYear, create);
                } catch (Exception ex) {
                    LOGGER.error("Unable to find certification ID By Listings", ex);
                    return null;
                }
            })
            .filter(certIdResult -> certIdResult != null)
            .collect(Collectors.toList());

        return resultsForAllYears;
    }

    private CertificationIdResults findCertificationIdByListings(List<CertifiedProductDetailsForCertificationId> listings, String certYear, boolean create)
        throws InvalidArgumentsException, CertificationIdException {
     // Add products to results
        CertificationIdResults result = new CertificationIdResults();
        result.setProducts(listings.stream()
                .map(listing -> new CertificationIdResults.Product(listing))
                .collect(Collectors.toList()));
        //get the "year" for this cms id
        result.setYear(!StringUtils.isEmpty(certYear) ? certYear : certIdYearCalculator.getCurrentCertIdYear());

        // Validate the collection
        //this will throw an error if an invalid year is passed in
        Validator validator = this.validatorFactory.getValidator(result.getYear());
        validator.getListings().addAll(listings);

        boolean isValid = validator.validate();
        result.setValid(isValid);
        result.setMetPercentages(validator.getPercents());
        result.setMetCounts(validator.getCounts());
        result.setMissingCombo(validator.getMissingCombo());
        result.setMissingOr(validator.getMissingOr());
        result.setMissingAnd(validator.getMissingAnd());
        result.setMissingXOr(validator.getMissingXOr());
        result.setMissingUpToDate(validator.getMissingUpToDate());

        // Lookup CERT ID
        if (validator.isValid()) {
            List<Long> listingIds = listings.stream()
                    .map(listing -> listing.getId())
                    .toList();
            CertificationIdDTO existingCertId = certificationIdManager.getByListings(listingIds, result.getYear());
            if (existingCertId != null) {
                result.setEhrCertificationId(existingCertId.getCertificationId());
            } else if (create && result.isValid()) {
                // Generate a new ID
                try {
                    existingCertId = certificationIdManager.create(listingIds, result.getYear());
                    result.setEhrCertificationId(existingCertId.getCertificationId());
                } catch (Exception ex) {
                    LOGGER.error("Could not create a CMS ID", ex);
                    throw new CertificationIdException("There was an error creating the CMS ID.");
                }
            }
        }
        return result;
    }

    private List<CertifiedProductDetailsForCertificationId> getAllListingDetails(List<Long> listingIds) {
        return listingIds.stream()
            .map(listingId -> getCertifiedProductDetailsForCertificationId(listingId))
            .filter(detailsOpt -> detailsOpt != null  && detailsOpt.isPresent())
            .map(detailsOpt -> detailsOpt.get())
            .collect(Collectors.toList());
    }

    protected Optional<CertifiedProductDetailsForCertificationId> getCertifiedProductDetailsForCertificationId(Long listingId) {
        try {
            return Optional.of(cpdManager.getCertifiedProductDetailsForCertificationId(listingId));
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not retrieve listing: %s", listingId), e);
            return Optional.empty();
        }
    }

    private boolean isEditionlessOrCuresUpdate(CertifiedProductDetailsForCertificationId listing) {
        if (listing.getYear() == null && listing.getCuresUpdate() == null) {
            return true;
        }
        return listing.getYear().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                && BooleanUtils.isTrue(listing.getCuresUpdate());
    }
}
