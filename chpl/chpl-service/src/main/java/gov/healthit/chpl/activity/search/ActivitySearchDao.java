package gov.healthit.chpl.activity.search;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
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

    public ActivitySearchDao(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil) {
        this.chplUserToCognitoUserUtil = chplUserToCognitoUserUtil;
    }

    @Transactional
    public List<ActivitySearchResult> findActivities(SearchRequest searchRequest) {

        String queryStr = "SELECT a "
                + "FROM ActivityEntity a "
                + "JOIN FETCH a.concept c "
                + "WHERE a.deleted = false ";
        if (!CollectionUtils.isEmpty(searchRequest.getTypes())) {
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

        if (!CollectionUtils.isEmpty(searchRequest.getTypes())) {
            query.setParameter("conceptNames", searchRequest.getTypes());
        }
        if (searchRequest.getActivityDateStart() != null) {
            query.setParameter("startDate", searchRequest.getActivityDateStart());
        }
        if (searchRequest.getActivityDateEnd() != null) {
            query.setParameter("endDate", searchRequest.getActivityDateEnd());
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
        if (!CollectionUtils.isEmpty(searchRequest.getTypes())) {
            queryStr += " AND (ac.concept IN (:conceptNames))";
        }
        if (searchRequest.getActivityDateStart() != null) {
            queryStr += " AND (ae.activityDate >= :startDate) ";
        }
        if (searchRequest.getActivityDateEnd() != null) {
            queryStr += " AND (ae.activityDate <= :endDate) ";
        }
        Query query = entityManager.createQuery(queryStr, Long.class);
        if (!CollectionUtils.isEmpty(searchRequest.getTypes())) {
            query.setParameter("conceptNames", searchRequest.getTypes());
        }
        if (searchRequest.getActivityDateStart() != null) {
            query.setParameter("startDate", searchRequest.getActivityDateStart());
        }
        if (searchRequest.getActivityDateEnd() != null) {
            query.setParameter("endDate", searchRequest.getActivityDateEnd());
        }
        return (Long) query.getSingleResult();
    }
}
