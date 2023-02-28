package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;

@Component
public class CheckInReportValidation {
	private static final Integer MAX_PAGE_SIZE = 100;

	private static final String RWT_VALIDATION_TRUE = "Has listing(s) with RWT criteria";
	private static final String RWT_VALIDATION_FALSE = "No listings with RWT criteria";
	private static final String ASSURANCES_VALIDATION_TRUE = "Has listing(s) with Assurances criteria (b)(6) or (b)(10)";
	private static final String ASSURANCES_VALIDATION_FALSE = "No listings with Assurances criteria (b)(6) or (b)(10)";
	private static final String API_VALIDATION_TRUE = "Has listing(s) with API criteria (g)(7)-(g)(10)";
	private static final String API_VALIDATION_FALSE = "No listings with API criteria (g)(7)-(g)(10)";

	private ListingSearchService listingSearchService;

	private List<CertificationCriterion> assurancesCriteria;
	private List<CertificationCriterion> apiCriteria;
	private List<CertificationCriterion> rwtCriteria;

	private Set<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(), CertificationStatusType.SuspendedByAcb.getName(), CertificationStatusType.SuspendedByOnc.getName()).collect(Collectors.toSet());

	@Autowired
	public CheckInReportValidation(RealWorldTestingCriteriaService realWorldTestingCriteriaService, CertificationCriterionService certificationCriterionService, ListingSearchService listingSearchService,
			@Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys, @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

		Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
		rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);

		assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());

		apiCriteria = Arrays.asList(apiCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());

	}

	public String getRealWorldTestingValidation(Developer developer, Logger logger) {
		List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, rwtCriteria, logger);
		if (!CollectionUtils.isEmpty(apiEligibleListings)) {
			return RWT_VALIDATION_TRUE;
		} else {
			return RWT_VALIDATION_FALSE;
		}
	}

	public String getAssurancesValidation(Developer developer, Logger logger) {
		List<ListingSearchResult> assurancesEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, assurancesCriteria, logger);
		if (!CollectionUtils.isEmpty(assurancesEligibleListings)) {
			return ASSURANCES_VALIDATION_TRUE;
		} else {
			return ASSURANCES_VALIDATION_FALSE;
		}
	}

	public String getApiValidation(Developer developer, Logger logger) {
		List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, apiCriteria, logger);
		if (!CollectionUtils.isEmpty(apiEligibleListings)) {
			return API_VALIDATION_TRUE;
		} else {
			return API_VALIDATION_FALSE;
		}
	}

	private List<ListingSearchResult> getActiveListingDataWithAnyCriteriaForDeveloper(Developer developer, List<CertificationCriterion> criteria, Logger logger) {
		SearchRequest searchRequest = SearchRequest.builder().certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet())).developer(developer.getName())
				.certificationStatuses(activeStatuses).certificationCriteriaIds(criteria.stream().map(criterion -> criterion.getId()).collect(Collectors.toSet())).certificationCriteriaOperator(SearchSetOperator.OR).pageSize(MAX_PAGE_SIZE)
				.pageNumber(0).build();
		return listingSearchService.getAllPagesOfSearchResults(searchRequest, logger);
	}

}
