package gov.healthit.chpl.targeteduser.search;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.targeteduser.TargetedUserDAO;
import gov.healthit.chpl.targeteduser.TargetedUserWithUsage;
import gov.healthit.chpl.targeteduser.TargetedUserWithUsage.UsageByCertificationStatus;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("targetedUserSearchService")
@NoArgsConstructor
@Log4j2
public class TargetedUserSearchService {
    private SearchRequestValidator searchRequestValidator;
    private SearchRequestNormalizer searchRequestNormalizer;
    private TargetedUserDAO targetedUserDao;
    private DateTimeFormatter dateFormatter;

    @Autowired
    public TargetedUserSearchService(TargetedUserDAO targetedUserDao,
            @Qualifier("targetedUserSearchRequestValidator") SearchRequestValidator searchRequestValidator) {
        this.targetedUserDao = targetedUserDao;
        this.searchRequestValidator = searchRequestValidator;
        this.searchRequestNormalizer = new SearchRequestNormalizer();
        dateFormatter = DateTimeFormatter.ofPattern(SearchRequest.DATE_SEARCH_FORMAT);
    }

    @Transactional(readOnly = true)
    public TargetedUserSearchResponse searchTargetedUsers(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<TargetedUserWithUsage> allTargetedUserResults = targetedUserDao.getAllWithUsage();
        LOGGER.debug("Total targeted users: " + allTargetedUserResults.size());
        List<TargetedUserWithUsage> matchedTargetedUsers = allTargetedUserResults.stream()
            .filter(tu -> matchesSearchTerm(tu, searchRequest.getSearchTerm()))
            .filter(tu -> matchesIsUsed(tu, searchRequest.getIsUsed()))
            .filter(tu -> matchesCreationDateRange(tu, searchRequest.getCreationDateStart(), searchRequest.getCreationDateEnd()))
            .collect(Collectors.toList());
        LOGGER.debug("Total matched targeted users: " + matchedTargetedUsers.size());

        TargetedUserSearchResponse response = new TargetedUserSearchResponse();
        response.setRecordCount(matchedTargetedUsers.size());
        response.setPageNumber(searchRequest.getPageNumber());
        response.setPageSize(searchRequest.getPageSize());

        sort(matchedTargetedUsers, searchRequest.getOrderBy(), searchRequest.getSortDescending());
        List<TargetedUserWithUsage> pageOfTargetedUsers
            = getPage(matchedTargetedUsers, getBeginIndex(searchRequest), getEndIndex(searchRequest));
        response.setResults(pageOfTargetedUsers);
        return response;
    }

    @Transactional(readOnly = true)
    public List<TargetedUserWithUsage> getFilteredTargetedUsers(SearchRequest searchRequest) throws ValidationException {
        searchRequestNormalizer.normalize(searchRequest);
        searchRequestValidator.validate(searchRequest);

        List<TargetedUserWithUsage> allTargetedUserResults = targetedUserDao.getAllWithUsage();
        LOGGER.debug("Total targeted users: " + allTargetedUserResults.size());
        List<TargetedUserWithUsage> matchedTargetedUsers = allTargetedUserResults.stream()
                .filter(tu -> matchesSearchTerm(tu, searchRequest.getSearchTerm()))
                .filter(tu -> matchesIsUsed(tu, searchRequest.getIsUsed()))
                .filter(tu -> matchesCreationDateRange(tu, searchRequest.getCreationDateStart(), searchRequest.getCreationDateEnd()))
                .collect(Collectors.toList());
            LOGGER.debug("Total matched targeted users: " + matchedTargetedUsers.size());
        return matchedTargetedUsers;
    }

    private boolean matchesSearchTerm(TargetedUserWithUsage tu, String searchTerm) {
        return matchesTargetedUserName(tu, searchTerm);
    }

    private boolean matchesTargetedUserName(TargetedUserWithUsage tu, String targetedUserName) {
        if (StringUtils.isEmpty(targetedUserName)) {
            return true;
        }

        return !StringUtils.isEmpty(tu.getName())
                && tu.getName().toUpperCase().contains(targetedUserName.toUpperCase());
    }

    private boolean matchesIsUsed(TargetedUserWithUsage tu, String isUsed) {
        if (isUsed == null) {
            return true;
        }

        Boolean isUsedBoolean = BooleanUtils.toBooleanObject(isUsed);
        if (isUsedBoolean) {
            return tu.getUsage().stream()
                    .filter(usage -> usage.getListingCount() > 0)
                    .findAny()
                    .isPresent();
        } else {
            return tu.getUsage().stream()
                    .filter(usage -> usage.getListingCount() > 0)
                    .findAny()
                    .isEmpty();
        }
    }

    private boolean matchesCreationDateRange(TargetedUserWithUsage tu, String creationDateRangeStart,
            String creationDateRangeEnd) {
        if (StringUtils.isAllEmpty(creationDateRangeStart, creationDateRangeEnd)) {
            return true;
        }
        LocalDate startDate = null, endDate = null;
        if (!StringUtils.isEmpty(creationDateRangeStart)) {
            startDate = parseLocalDate(creationDateRangeStart);
        }
        if (!StringUtils.isEmpty(creationDateRangeEnd)) {
            endDate = parseLocalDate(creationDateRangeEnd);
        }
        if (tu.getCreationDate() != null) {
            if (startDate == null && endDate != null) {
                return tu.getCreationDate().isEqual(endDate) || tu.getCreationDate().isBefore(endDate);
            } else if (startDate != null && endDate == null) {
                return tu.getCreationDate().isEqual(startDate) || tu.getCreationDate().isAfter(startDate);
            } else {
                return (tu.getCreationDate().isEqual(endDate) || tu.getCreationDate().isBefore(endDate))
                        && (tu.getCreationDate().isEqual(startDate) || tu.getCreationDate().isAfter(startDate));
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

    private List<TargetedUserWithUsage> getPage(List<TargetedUserWithUsage> targetedUsers, int beginIndex, int endIndex) {
        if (endIndex > targetedUsers.size()) {
            endIndex = targetedUsers.size();
        }
        if (endIndex <= beginIndex) {
            return new ArrayList<TargetedUserWithUsage>();
        }
        LOGGER.debug("Getting filtered targeted user results between [" + beginIndex + ", " + endIndex + ")");
        return targetedUsers.subList(beginIndex, endIndex);
    }

    private int getBeginIndex(SearchRequest searchRequest) {
        return searchRequest.getPageNumber() * searchRequest.getPageSize();
    }

    private int getEndIndex(SearchRequest searchRequest) {
        return getBeginIndex(searchRequest) + searchRequest.getPageSize();
    }

    private void sort(List<TargetedUserWithUsage> targetedUsers, OrderByOption orderBy, boolean descending) {
        if (orderBy == null) {
            return;
        }

        switch (orderBy) {
            case NAME:
                targetedUsers.sort(new TargetedUserNameComparator(descending));
                break;
            case USAGE_COUNT:
                targetedUsers.sort(new UsageCountComparator(descending));
                break;
            case CREATION_DATE:
                targetedUsers.sort(new CreationDateComparator(descending));
                break;
            default:
                LOGGER.error("Unrecognized value for Order By: " + orderBy.name());
                break;
        }
    }

    private class CreationDateComparator implements Comparator<TargetedUserWithUsage> {
        private boolean descending = false;

        CreationDateComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(TargetedUserWithUsage tu1, TargetedUserWithUsage tu2) {
            if (tu1.getCreationDate() == null ||  tu2.getCreationDate() == null) {
                return 0;
            }
            int sortFactor = descending ? -1 : 1;
            return (tu1.getCreationDate().compareTo(tu2.getCreationDate())) * sortFactor;
        }
    }

    private class TargetedUserNameComparator implements Comparator<TargetedUserWithUsage> {
        private boolean descending = false;

        TargetedUserNameComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(TargetedUserWithUsage tu1, TargetedUserWithUsage tu2) {
            String firstToCompare = "", secondToCompare = "";
            if (tu1.getName() != null) {
                firstToCompare = tu1.getName().toUpperCase();
            }
            if (tu2.getName() != null) {
                secondToCompare = tu2.getName().toUpperCase();
            }

            int sortFactor = descending ? -1 : 1;
            return (firstToCompare.compareTo(secondToCompare)) * sortFactor;
        }
    }

    private class UsageCountComparator implements Comparator<TargetedUserWithUsage> {
        private boolean descending = false;

        UsageCountComparator(boolean descending) {
            this.descending = descending;
        }

        @Override
        public int compare(TargetedUserWithUsage tu1, TargetedUserWithUsage tu2) {
            Long tu1TotalUsage = tu1.getUsage().stream()
                .mapToLong(UsageByCertificationStatus::getListingCount)
                .sum();
            Long tu2TotalUsage = tu2.getUsage().stream()
                    .mapToLong(UsageByCertificationStatus::getListingCount)
                    .sum();

            int sortFactor = descending ? -1 : 1;
            return (tu1TotalUsage.compareTo(tu2TotalUsage)) * sortFactor;
        }
    }
}
