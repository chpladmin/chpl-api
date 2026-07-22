package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport.CriterionAndSvapData;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class CheckInReportRwtResultsDataCollector {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ListingSearchService listingSearchService;
    private CertificationCriterionService criteriaService;
    private List<String> activeStatuses = CertificationStatusUtil.getActiveStatusNames();

    @Autowired
    public CheckInReportRwtResultsDataCollector(ListingSearchService listingSearchService,
            CertificationCriterionService criteriaService) {
        this.listingSearchService = listingSearchService;
        this.criteriaService = criteriaService;
    }

    public List<CriterionAndSvapData> collect(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingsForDeveloper(developerId);
        return buildCriteriaAndSvapData(allActiveListingsForDeveloper);
    }

    private List<ListingSearchResult> getActiveListingsForDeveloper(Long developerId) {
        LOGGER.info("Getting all active listings");
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationStatuses(activeStatuses.stream().collect(Collectors.toSet()))
                .developerId(developerId)
                .pageSize(MAX_PAGE_SIZE)
                .pageNumber(0)
                .build();
        return listingSearchService.getAllPagesOfSearchResults(searchRequest, LOGGER);
    }

    private List<CriterionAndSvapData> buildCriteriaAndSvapData(List<ListingSearchResult> allActiveListingsForDeveloper) {
        List<Long> allAttestedCriterionIds = allActiveListingsForDeveloper.stream()
            .flatMap(listingSearchResult -> listingSearchResult.getCriteriaMet().stream())
            .map(item -> item.getId())
            .distinct()
            .collect(Collectors.toList());

        return allAttestedCriterionIds.stream()
            .map(attestedCriterionId -> buildCriterionAndSvapData(criteriaService.get(attestedCriterionId), allActiveListingsForDeveloper))
            .collect(Collectors.toList());
    }

    private CriterionAndSvapData buildCriterionAndSvapData(CertificationCriterion criterion, List<ListingSearchResult> listings) {
        return CriterionAndSvapData.builder()
                    .criterion(criterion)
                    .isAttested(true)
                    .isGCriterion(criteriaService.isGCriterion(criterion))
                    .usesSvap(doesAnyListingUseSvapWithCriterion(criterion, listings))
                    .build();
    }

    private boolean doesAnyListingUseSvapWithCriterion(CertificationCriterion criterion, List<ListingSearchResult> listings) {
        return listings.stream()
            .filter(listing -> listing.getCriteriaMet().stream().filter(criterionMet -> criterionMet.getId().equals(criterion.getId())).findAny().isPresent())
            .filter(listingWithCriterion -> listingWithCriterion.getSvaps().stream().filter(svap -> svap.getCriterion().getId().equals(criterion.getId())).findAny().isPresent())
            .findAny()
            .isPresent();
    }
}
