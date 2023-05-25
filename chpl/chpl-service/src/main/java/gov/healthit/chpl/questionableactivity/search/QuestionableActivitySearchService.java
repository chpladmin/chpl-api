package gov.healthit.chpl.questionableactivity.search;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
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

    private List<QuestionableActivityTrigger> allTriggerTypes;

    @Autowired
    public QuestionableActivitySearchService(QuestionableActivitySearchDAO questionableActivitySearchDao,
            QuestionableActivityDAO questionableActivityDao,
            @Qualifier("questionableActivitySearchRequestValidator") SearchRequestValidator searchRequestValidator) {
        this.questionableActivitySearchDao = questionableActivitySearchDao;
        this.searchRequestValidator = searchRequestValidator;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
        this.allTriggerTypes = questionableActivityDao.getAllTriggers();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QUESTIONABLE_ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.QuestionableActivityDomainPermissions).GET)")
    public QuestionableActivitySearchResponse searchQuestionableActivities(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<QuestionableActivitySearchResult> allQuestionableActivities = questionableActivitySearchDao.getAll();
        LOGGER.debug("Total questionable activities: " + allQuestionableActivities.size());
        List<QuestionableActivitySearchResult> matchedQuestionableActivities = allQuestionableActivities.stream()
            .filter(qa -> matchesSearchTerm(qa, searchRequest.getSearchTerm()))
            .filter(qa -> matchesTriggers(qa, searchRequest.getTriggerIds()))
            .filter(qa -> matchesActivityDateRange(qa, searchRequest.getActivityDateStart(), searchRequest.getActivityDateEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched questionable activities: " + matchedQuestionableActivities.size());

        QuestionableActivitySearchResponse response = new QuestionableActivitySearchResponse();
        response.setRecordCount(matchedQuestionableActivities.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

        sort(matchedQuestionableActivities, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<QuestionableActivitySearchResult> pageOfQuestionableActivity
            = getPage(matchedQuestionableActivities, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfQuestionableActivity);
        return response;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).QUESTIONABLE_ACTIVITY, "
            + "T(gov.healthit.chpl.permissions.domains.QuestionableActivityDomainPermissions).GET)")
    public List<QuestionableActivitySearchResult> getFilteredQuestionableActivities(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<QuestionableActivitySearchResult> allQuestionableActivities = questionableActivitySearchDao.getAll();
        LOGGER.debug("Total questionable activities: " + allQuestionableActivities.size());
        List<QuestionableActivitySearchResult> matchedQuestionableActivities = allQuestionableActivities.stream()
            .filter(qa -> matchesSearchTerm(qa, searchRequest.getSearchTerm()))
            .filter(qa -> matchesTriggers(qa, searchRequest.getTriggerIds()))
            .filter(qa -> matchesActivityDateRange(qa, searchRequest.getActivityDateStart(), searchRequest.getActivityDateEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched questionable activities: " + matchedQuestionableActivities.size());
        return matchedQuestionableActivities;
    }

    private boolean matchesSearchTerm(QuestionableActivitySearchResult qa, String searchTerm) {
        return matchesDeveloperName(qa, searchTerm)
                || matchesProductName(qa, searchTerm)
                || matchesChplProductNumber(qa, searchTerm);
    }

    private boolean matchesDeveloperName(QuestionableActivitySearchResult qa, String developerName) {
        if (StringUtils.isEmpty(developerName)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getDeveloperName())
                && qa.getDeveloperName().toUpperCase().contains(developerName.toUpperCase());
    }

    private boolean matchesProductName(QuestionableActivitySearchResult qa, String productName) {
        if (StringUtils.isEmpty(productName)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getProductName())
                && qa.getProductName().toUpperCase().contains(productName.toUpperCase());
    }

    private boolean matchesChplProductNumber(QuestionableActivitySearchResult qa, String chplProductNumber) {
        if (StringUtils.isEmpty(chplProductNumber)) {
            return true;
        }

        return !StringUtils.isEmpty(qa.getChplProductNumber())
                && qa.getChplProductNumber().toUpperCase().contains(chplProductNumber.toUpperCase());
    }

    private boolean matchesTriggers(QuestionableActivitySearchResult qa, Set<Long> triggerIds) {
        if (CollectionUtils.isEmpty(triggerIds)) {
            return true;
        }
        return triggerIds.stream()
                    .anyMatch(triggerId -> getTriggerId(qa.getTriggerLevel(), qa.getTriggerName()).equals(triggerId));
    }

    private Long getTriggerId(String triggerLevel, String triggerName) {
        Optional<QuestionableActivityTrigger> matchingTrigger = allTriggerTypes.stream()
                .filter(trigger -> trigger.getLevel().equals(triggerLevel)
                        && trigger.getName().equals(triggerName))
                .findAny();
        if (matchingTrigger.isEmpty()) {
            return 0L;
        }
        return matchingTrigger.get().getId();
    }

    private boolean matchesActivityDateRange(QuestionableActivitySearchResult qa, String activityDateRangeStart,
            String activityDateRangeEnd) {
        if (StringUtils.isAllEmpty(activityDateRangeStart, activityDateRangeEnd)) {
            return true;
        }
        LocalDateTime startDate = null, endDate = null;
        if (!StringUtils.isEmpty(activityDateRangeStart)) {
            startDate = parseLocalDate(activityDateRangeStart).atStartOfDay();
        }
        if (!StringUtils.isEmpty(activityDateRangeEnd)) {
            endDate = parseLocalDate(activityDateRangeEnd).atTime(LocalTime.MAX);
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

    private List<QuestionableActivitySearchResult> getPage(List<QuestionableActivitySearchResult> activities, int beginIndex, int endIndex) {
        if (endIndex > activities.size()) {
            endIndex = activities.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<QuestionableActivitySearchResult>();
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

    private void sort(List<QuestionableActivitySearchResult> activities, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case ACTIVITY_DATE:
                activities.sort(new ActivityDateComparator(descending));
                break;
            case DEVELOPER:
                activities.sort(new DeveloperNameComparator(descending));
                break;
            case PRODUCT:
                activities.sort(new ProductNameComparator(descending));
                break;
            case VERSION:
                activities.sort(new VersionComparator(descending));
                break;
            case CHPL_PRODUCT_NUMBER:
                activities.sort(new ChplProductNumberComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class ActivityDateComparator implements Comparator<QuestionableActivitySearchResult> {
        private boolean descending = false;

        ActivityDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivitySearchResult qa1, QuestionableActivitySearchResult qa2) {
            if (qa1.getActivityDate() == null ||  qa2.getActivityDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (qa1.getActivityDate().compareTo(qa2.getActivityDate())) * sortFactor;
        }
    }

    private class DeveloperNameComparator implements Comparator<QuestionableActivitySearchResult> {
        private boolean descending = false;

        DeveloperNameComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivitySearchResult qa1, QuestionableActivitySearchResult qa2) {
            String firstToCompare = "", secondToCompare = "";
            if (qa1.getDeveloperName() != null) {
                firstToCompare = qa1.getDeveloperName().toUpperCase();
            }
            if (qa2.getDeveloperName() != null) {
                secondToCompare = qa2.getDeveloperName().toUpperCase();
            }

            int sortFactor = descending ? -1 : 1;
            return (firstToCompare.compareTo(secondToCompare)) * sortFactor;
        }
    }

    private class ProductNameComparator implements Comparator<QuestionableActivitySearchResult> {
        private boolean descending = false;

        ProductNameComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivitySearchResult qa1, QuestionableActivitySearchResult qa2) {
            String firstToCompare = "", secondToCompare = "";
            if (qa1.getProductName() != null) {
                firstToCompare = qa1.getProductName().toUpperCase();
            }
            if (qa2.getProductName() != null) {
                secondToCompare = qa2.getProductName().toUpperCase();
            }

            int sortFactor = descending ? -1 : 1;
            return (firstToCompare.compareTo(secondToCompare)) * sortFactor;
        }
    }

    private class VersionComparator implements Comparator<QuestionableActivitySearchResult> {
        private boolean descending = false;

        VersionComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivitySearchResult qa1, QuestionableActivitySearchResult qa2) {
            String firstToCompare = "", secondToCompare = "";
            if (qa1.getVersionName() != null) {
                firstToCompare = qa1.getVersionName().toUpperCase();
            }
            if (qa2.getVersionName() != null) {
                secondToCompare = qa2.getVersionName().toUpperCase();
            }

            int sortFactor = descending ? -1 : 1;
            return (firstToCompare.compareTo(secondToCompare)) * sortFactor;
        }
    }

    private class ChplProductNumberComparator implements Comparator<QuestionableActivitySearchResult> {
        private boolean descending = false;

        ChplProductNumberComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(QuestionableActivitySearchResult qa1, QuestionableActivitySearchResult qa2) {
            String firstToCompare = "", secondToCompare = "";
            if (qa1.getChplProductNumber() != null) {
                firstToCompare = qa1.getChplProductNumber().toUpperCase();
            }
            if (qa2.getChplProductNumber() != null) {
                secondToCompare = qa2.getChplProductNumber().toUpperCase();
            }

            int sortFactor = descending ? -1 : 1;
            return (firstToCompare.compareTo(secondToCompare)) * sortFactor;
        }
    }
}
