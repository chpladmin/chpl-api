package gov.healthit.chpl.activity.search;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.ValidationException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("activitySearchService")
@NoArgsConstructor
@Log4j2
public class ActivitySearchService {
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private ActivitySearchDao activitySearchDao;

    @Autowired
    public ActivitySearchService(ActivitySearchDao activitySearchDao,
            @Qualifier("activitySearchRequestValidator") SearchRequestValidator searchRequestValidator) {
        this.activitySearchDao = activitySearchDao;
        this.searchRequestValidator = searchRequestValidator;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.ActivityDomainPermissions).SEARCH)")
    public ActivitySearchResponse searchActivities(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<ActivitySearchResult> matchedActivities = activitySearchDao.findActivities(searchRequest);
        Long totalMatchedActivityCount = activitySearchDao.getTotalActivityCount(searchRequest);

        ActivitySearchResponse response = new ActivitySearchResponse();
        response.setRecordCount(totalMatchedActivityCount);
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());
        response.setResults(matchedActivities);
        return response;
    }
}
