package gov.healthit.chpl.attestation.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.scheduler.job.developer.attestation.AttestationFormMetaData;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.service.ListingApplicabilityService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AttestationResponseValidationService {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ListingApplicabilityService listingApplicabilityService;
    private ListingSearchService listingSearchService;

    private Set<String> activeStatuses = Stream.of(
            CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toSet());

    public AttestationResponseValidationService(ListingApplicabilityService listingApplicabilityService,
            ListingSearchService listingSearchService) {
        this.listingApplicabilityService = listingApplicabilityService;
        this.listingSearchService = listingSearchService;
    }

    public Boolean isApiApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isApiApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isApiNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isApiNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isApiApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getNotApplicableResponseId());
    }

    public Boolean isApiNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getCompliantResponseId());
    }

    public Boolean isAssurancesApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm, Long attestationPeriodId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isAssurancesApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm, attestationPeriodId);
    }

    public Boolean isAssurancesNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm, Long attestationPeriodId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isAssurancesNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm, attestationPeriodId);
    }

    public Boolean isAssurancesApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        return listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getAssurancesCompliantIsNotApplicableResponseId(attestationPeriodId));
    }

    public Boolean isAssurancesNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        return !listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getAssurancesCompliantIsApplicableResponseId(attestationPeriodId));
    }

    public Boolean isRwtApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isRwtApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isRwtNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isRwtNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isRwtApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getNotApplicableResponseId());
    }

    public Boolean isRwtNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getCompliantResponseId());
    }

    public Boolean doesFormResponseEqualResponse(Form attestationForm, Long conditionIdToCheck, Long expectedResult) {
        return attestationForm.extractFlatFormItems().stream()
                .filter(fi -> fi.getQuestion().getId().equals(conditionIdToCheck)
                        && fi.getSubmittedResponses().stream()
                                .filter(sr -> sr.getId().equals(expectedResult))
                                .findAny().isPresent())
                .findAny()
                .isPresent();
    }

    private List<ListingSearchResult> getActiveListingDataForDeveloper(Long developerId) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developerId(developerId)
                    .certificationStatuses(activeStatuses)
                    .pageSize(MAX_PAGE_SIZE)
                    .pageNumber(0)
                    .build();
            List<ListingSearchResult> searchResults = listingSearchService.getAllPagesOfSearchResults(searchRequest);
            return searchResults;
        } catch (ValidationException ex) {
            LOGGER.error("Could not retrieve listings from search request.", ex);
            return null;
        }
    }
}
