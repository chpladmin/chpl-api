package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityCreationException;
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
                List<CertifiedProductSearchDetails> listings = getAllListingDetails(listingIds);
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

    public CertificationIdResults findCertificationByListingIds(List<Long> listingIds, String certificationYear, Boolean create)
            throws InvalidArgumentsException, EntityRetrievalException, CertificationIdException {
        if (CollectionUtils.isEmpty(listingIds)) {
            return null;
        }

        List<CertifiedProductSearchDetails> listings = getAllListingDetails(listingIds);

        if (create) {
            Optional<CertifiedProductSearchDetails> invalidListing = listings.stream()
                .filter(listing -> !isEditionlessOrCuresUpdate(listing))
                .findAny();
            if (invalidListing.isPresent()) {
                throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Cures Update Listings");
            }
        }

        // Add products to results
        CertificationIdResults results = new CertificationIdResults();
        results.setProducts(listings.stream()
                .map(listing -> new CertificationIdResults.Product(listing))
                .collect(Collectors.toList()));
        //get the "year" for this cms id
        results.setYear(!StringUtils.isEmpty(certificationYear) ? certificationYear : certIdYearCalculator.getCurrentCertIdYear());

        // Validate the collection
        //this will throw an error if an invalid year is passed in
        Validator validator = this.validatorFactory.getValidator(results.getYear());
        validator.getListings().addAll(listings);

        boolean isValid = validator.validate();
        results.setValid(isValid);
        results.setMetPercentages(validator.getPercents());
        results.setMetCounts(validator.getCounts());
        results.setMissingCombo(validator.getMissingCombo());
        results.setMissingOr(validator.getMissingOr());
        results.setMissingAnd(validator.getMissingAnd());
        results.setMissingXOr(validator.getMissingXOr());
        results.setMissingUpToDate(validator.getMissingUpToDate());

        // Lookup CERT ID
        if (validator.isValid()) {
            CertificationIdDTO existingCertId = null;
            try {
                existingCertId = certificationIdManager.getByListings(listingIds, results.getYear());
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

    private List<CertifiedProductSearchDetails> getAllListingDetails(List<Long> listingIds) {
        LOGGER.info("Getting all listing details for cert id search");
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<CompletableFuture<Optional<CertifiedProductSearchDetails>>> futures = new ArrayList<CompletableFuture<Optional<CertifiedProductSearchDetails>>>();
        listingIds.stream()
            .forEach(listingId -> futures.add(CompletableFuture
                    .supplyAsync(() -> getCertifiedProductSearchDetails(listingId), executorService)));

        CompletableFuture<?>[] futuresArray = futures.toArray(new CompletableFuture<?>[0]);
        CompletableFuture<List<Optional<CertifiedProductSearchDetails>>> listFuture = CompletableFuture.allOf(futuresArray)
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<CertifiedProductSearchDetails> listings = listFuture.join().stream()
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());
        try {
            executorService.close();
        } catch (Exception ex) {
            LOGGER.error("Executor service did not properly close", ex);
        }
        LOGGER.info("Got all listing details for cert id search");
        return listings;
    }

    protected Optional<CertifiedProductSearchDetails> getCertifiedProductSearchDetails(Long listingId) {
        try {
            return Optional.of(cpdManager.getCertifiedProductDetails(listingId));
        } catch (EntityRetrievalException e) {
            LOGGER.error(String.format("Could not retrieve listing: %s", listingId), e);
            return Optional.empty();
        }
    }

    private boolean isEditionlessOrCuresUpdate(CertifiedProductSearchDetails listing) {
        if (listing.getEdition() == null && listing.getCuresUpdate() == null) {
            return true;
        }
        return listing.getEdition().getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())
                && BooleanUtils.isTrue(listing.getCuresUpdate());
    }
}
