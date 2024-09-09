package gov.healthit.chpl.activity.search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("activitySearchDao")
public class ActivitySearchDao extends BaseDAOImpl {

    private ChplUserToCognitoUserUtil chplUserToCognitoUserUtil;
    private DateTimeFormatter dateFormatter;

    public ActivitySearchDao(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil) {
        this.chplUserToCognitoUserUtil = chplUserToCognitoUserUtil;
        this.dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    @Transactional
    public List<ActivitySearchResult> findActivities(SearchRequest searchRequest) {

        String queryStr = "SELECT a "
                + "FROM ActivityEntity a "
                + "JOIN FETCH a.concept c "
                + "WHERE a.deleted = false ";
        if (!CollectionUtils.isEmpty(searchRequest.getConcepts())) {
            queryStr += " AND c.concept IN (:conceptNames) ";
        }
        if (searchRequest.getActivityDateStart() != null) {
            queryStr += " AND (a.activityDate >= :startDate) ";
        }
        if (searchRequest.getActivityDateEnd() != null) {
            queryStr += " AND (a.activityDate <= :endDate) ";
        }
        queryStr += " ORDER BY ";
        if (searchRequest.getOrderBy().equals(OrderByOption.ACTIVITY_DATE)) {
            queryStr += " a.activityDate ";
        } else {
            queryStr += " a.id ";
        }
        if (BooleanUtils.isTrue(searchRequest.getSortDescending())) {
            queryStr += " DESC ";
        } else {
            queryStr += " ASC ";
        }

        Query query = entityManager.createQuery(queryStr, ActivityEntity.class);

        int firstRecord = (searchRequest.getPageNumber() * searchRequest.getPageSize());
        query.setFirstResult(firstRecord);
        query.setMaxResults(searchRequest.getPageSize());

        if (!CollectionUtils.isEmpty(searchRequest.getConcepts())) {
            query.setParameter("conceptNames", searchRequest.getConcepts());
        }
        if (searchRequest.getActivityDateStart() != null) {
            LocalDateTime activityDateStart = parseLocalDateTime(searchRequest.getActivityDateStart());
            query.setParameter("startDate", activityDateStart);
        }
        if (searchRequest.getActivityDateEnd() != null) {
            LocalDateTime activityDateEnd = parseLocalDateTime(searchRequest.getActivityDateEnd());
            query.setParameter("endDate", activityDateEnd);
        }


        List<ActivityEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> {
                    ActivitySearchResult asr = entity.toSearchResult();
                    //TODO consider adding users
                    User user = chplUserToCognitoUserUtil.getUser(entity.getLastModifiedUser(), entity.getLastModifiedSsoUser());
                    if (user != null) {
                        asr.setUsername(ObjectUtils.firstNonNull(user.getEmail(), user.getFullName()));
                    }
                    return asr;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Long getTotalActivityCount(SearchRequest searchRequest) {
        String queryStr = "SELECT COUNT(ae) "
                + "FROM ActivityEntity ae "
                + "JOIN ae.concept ac "
                + "WHERE ae.deleted = false ";
        if (!CollectionUtils.isEmpty(searchRequest.getConcepts())) {
            queryStr += " AND (ac.concept IN (:conceptNames))";
        }
        if (searchRequest.getActivityDateStart() != null) {
            queryStr += " AND (ae.activityDate >= :startDate) ";
        }
        if (searchRequest.getActivityDateEnd() != null) {
            queryStr += " AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr, Long.class);
        if (!CollectionUtils.isEmpty(searchRequest.getConcepts())) {
            query.setParameter("conceptNames", searchRequest.getConcepts());
        }
        if (searchRequest.getActivityDateStart() != null) {
            LocalDateTime activityDateStart = parseLocalDateTime(searchRequest.getActivityDateStart());
            query.setParameter("startDate", activityDateStart);
        }
        if (searchRequest.getActivityDateEnd() != null) {
            LocalDateTime activityDateEnd = parseLocalDateTime(searchRequest.getActivityDateEnd());
            query.setParameter("endDate", activityDateEnd);
        }
        return (Long) query.getSingleResult();
    }

    private LocalDateTime parseLocalDateTime(String dateTimeString) {
        if (StringUtils.isEmpty(dateTimeString)) {
            return null;
        }

        LocalDateTime date = null;
        try {
            date = LocalDateTime.parse(dateTimeString, dateFormatter);
        } catch (DateTimeParseException ex) {
            LOGGER.error("Cannot parse " + dateTimeString + " as LocalDateTime of the format " + SearchRequest.TIMESTAMP_SEARCH_FORMAT);
        }
        return date;
    }
}
