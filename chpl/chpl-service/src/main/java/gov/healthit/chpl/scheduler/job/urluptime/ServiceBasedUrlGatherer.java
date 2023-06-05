package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ServiceBasedUrlGatherer {

    private ListingSearchService listingSearchService;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public ServiceBasedUrlGatherer(ListingSearchService listingSearchService, CertifiedProductDetailsManager certifiedProductDetailsManager,
            CertificationCriterionService certificationCriterionService) {
        this.listingSearchService = listingSearchService;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<ServiceBasedUrl> getAllServiceBasedUrls() {
        try {
            return reduceBasedOnUrl(findAllListingsWithG10Criteria());
        } catch (ValidationException e) {
            LOGGER.error("Could not perform listing search for (g)(10) criteria", e);
            return null;
        }

    }

    private List<ServiceBasedUrl> reduceBasedOnUrl(List<ListingSearchResult> listingSearchResults) {
        Map<String, ServiceBasedUrl> serviceBasedUrls = Map.of();

        listingSearchResults.forEach(result -> {
            CertifiedProductSearchDetails listing = getListing(result.getId());
            if (listing == null) {
                return;
            }
            CertificationResult certificationResult = getG10CriteriaResult(listing);
            if (certificationResult == null) {
                return;
            }

            if (serviceBasedUrls.containsKey(certificationResult.getServiceBaseUrlList())) {
                serviceBasedUrls.get(certificationResult.getServiceBaseUrlList()).getChplProductNumbers().add(listing.getChplProductNumber());
            } else {
                serviceBasedUrls.put(certificationResult.getServiceBaseUrlList(),
                        ServiceBasedUrl.builder()
                                .url(certificationResult.getServiceBaseUrlList())
                                .developerId(listing.getDeveloper().getId())
                                .chplProductNumbers(List.of(listing.getChplProductNumber()))
                                .build());
            }
        });
        return serviceBasedUrls.values().stream()
                .toList();

    }

    private List<ListingSearchResult> findAllListingsWithG10Criteria() throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(Set.of(CertificationStatusType.Active.getName(),
                        CertificationStatusType.SuspendedByAcb.getName(),
                        CertificationStatusType.SuspendedByOnc.getName()))
                .certificationCriteriaIds(Set.of(getG10Criteria().getId()))
                .certificationCriteriaOperator(SearchSetOperator.OR)
                .build();

        return listingSearchService.getAllPagesOfSearchResults(request);
    }

    private CertificationResult getG10CriteriaResult(CertifiedProductSearchDetails listing) {
        Optional<CertificationResult> certificationResult = listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(getG10Criteria().getId()))
                .findAny();

        if (certificationResult.isPresent()) {
            return certificationResult.get();
        } else {
            LOGGER.warn("Could not retrieve (g)(10) certification result from listing: {}", listing.getId());
            return null;
        }
    }

    private CertificationCriterion getG10Criteria() {
        return certificationCriterionService.get(CertificationCriterionService.Criteria2015.G_10);
    }

    private CertifiedProductSearchDetails getListing(Long id) {
        LOGGER.info("Retrieving listing: {}", id);
        try {
            return certifiedProductDetailsManager.getCertifiedProductDetails(id);
        } catch (EntityRetrievalException e) {
            LOGGER.warn("Could not retrieve listing: {}", id, e);
            return null;
        }
    }
 }
