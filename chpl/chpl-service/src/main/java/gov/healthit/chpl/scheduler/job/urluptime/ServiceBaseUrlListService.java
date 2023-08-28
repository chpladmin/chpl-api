package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBaseUrlListUptimeCreatorJobLogger")
@Component
public class ServiceBaseUrlListService {

    private ListingSearchService listingSearchService;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public ServiceBaseUrlListService(ListingSearchService listingSearchService, CertifiedProductDetailsManager certifiedProductDetailsManager,
            CertificationCriterionService certificationCriterionService) {
        this.listingSearchService = listingSearchService;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<ServiceBaseUrlList> getAllServiceBaseUrlLists() {
        try {
            return reduceBasedOnUrl(findAllListingsWithG10Criteria());
        } catch (ValidationException e) {
            LOGGER.error("Could not perform listing search for (g)(10) criteria", e);
            return null;
        }

    }

    private List<ServiceBaseUrlList> reduceBasedOnUrl(List<ListingSearchResult> listingSearchResults) {
        Map<String, ServiceBaseUrlList> serviceBaseUrlLists = new HashMap<String, ServiceBaseUrlList>();

        listingSearchResults.forEach(result -> {
            CertifiedProductSearchDetails listing = getListing(result.getId());
            if (listing == null) {
                return;
            }
            CertificationResult certificationResult = getG10CriteriaResult(listing);
            if (certificationResult == null) {
                return;
            }

            if (serviceBaseUrlLists.containsKey(certificationResult.getServiceBaseUrlList())) {
                serviceBaseUrlLists.get(certificationResult.getServiceBaseUrlList()).getChplProductNumbers().add(listing.getChplProductNumber());
            } else {
                serviceBaseUrlLists.put(certificationResult.getServiceBaseUrlList(),
                        ServiceBaseUrlList.builder()
                                .url(certificationResult.getServiceBaseUrlList())
                                .developerId(listing.getDeveloper().getId())
                                .chplProductNumbers(new ArrayList<String>(Arrays.asList(listing.getChplProductNumber())))
                                .build());
            }
        });
        return serviceBaseUrlLists.values().stream()
                .toList();

    }

    private List<ListingSearchResult> findAllListingsWithG10Criteria() throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet()))
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
