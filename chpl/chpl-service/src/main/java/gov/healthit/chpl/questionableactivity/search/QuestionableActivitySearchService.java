package gov.healthit.chpl.questionableactivity.search;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivity;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("questionableActivitySearchService")
@NoArgsConstructor
@Log4j2
public class QuestionableActivitySearchService {
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private QuestionableActivitySearchDAO questionableActivitySearchDao;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public QuestionableActivitySearchService(QuestionableActivitySearchDAO questionableActivitySearchDao,
            @Qualifier("questionableActivitySearchRequestValidator") SearchRequestValidator searchRequestValidator) {
        this.questionableActivitySearchDao = questionableActivitySearchDao;
        this.searchRequestValidator = searchRequestValidator;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QUESTIONABLE_ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.QuestionableActivityDomainPermissions).SEARCH)")
    public QuestionableActivitySearchResponse searchQuestionableActivities(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<QuestionableActivity> allQuestionableActivities = questionableActivitySearchDao.getAll();
        LOGGER.debug("Total questionable activities: " + allQuestionableActivities.size());
        List<QuestionableActivity> matchedQuestionableActivities = allQuestionableActivities.stream()
            .filter(qa -> matchesSearchTerm(qa, searchRequest.getSearchTerm()))
            .filter(qa -> matchesActivityDateRange(qa, searchRequest.getActivityDateStart(), searchRequest.getActivityDateEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched questionable activities: " + matchedQuestionableActivities.size());

        QuestionableActivitySearchResponse response = new QuestionableActivitySearchResponse();
        response.setRecordCount(matchedQuestionableActivities.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

        sort(matchedQuestionableActivities, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<QuestionableActivity> pageOfQuestionableActivity
            = getPage(matchedQuestionableActivities, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfQuestionableActivity);
        return response;
    }

    private boolean matchesSearchTerm(QuestionableActivity qa, String searchTerm) {
        return matchesDeveloperName(qa, searchTerm)
                || matchesProductName(qa, searchTerm)
                || matchesVersionName(qa, searchTerm)
                || matchesChplProductNumber(qa, searchTerm);
    }

    private boolean matchesDeveloperName(QuestionableActivity qa, String developerName) {
        if (StringUtils.isEmpty(developerName)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getDeveloperName())
                && qa.getDeveloperName().toUpperCase().contains(developerName.toUpperCase());
    }

    private boolean matchesProductName(QuestionableActivity qa, String productName) {
        if (StringUtils.isEmpty(productName)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getProductName())
                && qa.getProductName().toUpperCase().contains(productName.toUpperCase());
    }

    private boolean matchesVersionName(QuestionableActivity qa, String versionName) {
        if (StringUtils.isEmpty(versionName)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getVersionName())
                && qa.getVersionName().toUpperCase().contains(versionName.toUpperCase());
    }

    private boolean matchesChplProductNumber(QuestionableActivity qa, String chplProductNumber) {
        if (StringUtils.isEmpty(chplProductNumber)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getChplProductNumber())
                && qa.getChplProductNumber().toUpperCase().contains(chplProductNumber.toUpperCase());
    }

    private boolean matchesActivityDateRange(QuestionableActivity qa, String activityDateRangeStart,
            String activityDateRangeEnd) {
        if (StringUtils.isAllEmpty(activityDateRangeStart, activityDateRangeEnd)) {
            return true;
        }
        LocalDateTime startDate = null, endDate = null;
        if (!StringUtils.isEmpty(activityDateRangeStart)) {
            startDate = parseLocalDate(activityDateRangeStart).atStartOfDay();
        }
        if (!StringUtils.isEmpty(activityDateRangeEnd)) {
            endDate = parseLocalDate(activityDateRangeEnd).atTime(23, 59, 59, 999);
        }
        if (qa.getActivityDate() != null) {
            if (startDate == null && endDate != null) {
                return qa.getActivityDate().isEqual(endDate) || qa.getActivityDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return qa.getActivityDate().isEqual(startDate) || qa.getActivityDate().isAfter(startDate);
            } else {
                return (qa.getActivityDate().isEqual(endDate) || qa.getActivityDate().isBefore(endDate))
                        && (qa.getActivityDate().isEqual(startDate) || qa.getActivityDate().isAfter(startDate));
            }
        }
        return false;
    }

    private LocalDate parseLocalDate(String dateString) {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }

        LocalDate date = null;
        try {
            date = LocalDate.parse(dateString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + dateString + " as date of the format " + SearchRequest.DATE_SEARCH_FORMAT);
        }
        return date;
    }

    private List<QuestionableActivity> getPage(List<QuestionableActivity> activities, int beginIndex, int endIndex) {
        if (endIndex > activities.size()) {
            endIndex = activities.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<QuestionableActivity>();
        }
        LOGGER.debug("Getting filtered questionable activity results between [" + beginIndex + ", " + endIndex + ")");
        return activities.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(SearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(SearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<QuestionableActivity> activities, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case ACTIVITY_DATE:
                activities.sort(new ActivityDateComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class ActivityDateComparator implements Comparator<QuestionableActivity> {
        private boolean descending = false;

        ActivityDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivity qa1, QuestionableActivity qa2) {
            if (qa1.getActivityDate() == null ||  qa2.getActivityDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (qa1.getActivityDate().compareTo(qa2.getActivityDate())) * sortFactor;
        }
    }
}
