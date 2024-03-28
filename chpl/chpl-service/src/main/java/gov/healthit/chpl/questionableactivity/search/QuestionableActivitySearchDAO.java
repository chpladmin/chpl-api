package gov.healthit.chpl.questionableactivity.search;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivitySearchResultEntity;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("questionableActivitySearchDao")
public class QuestionableActivitySearchDAO extends BaseDAOImpl {

    private ChplUserToCognitoUserUtil chplUserToCognitoUserUtil;

    public QuestionableActivitySearchDAO(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil) {
        this.chplUserToCognitoUserUtil = chplUserToCognitoUserUtil;
    }

    @Transactional
    @Cacheable(cacheNames = CacheNames.QUESTIONABLE_ACTIVITIES)
    public List<QuestionableActivitySearchResult> getAll() {
        Query query = entityManager.createQuery("SELECT qa FROM QuestionableActivitySearchResultEntity qa ",
                QuestionableActivitySearchResultEntity.class);
        List<QuestionableActivitySearchResultEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> {
                    QuestionableActivitySearchResult qasr = entity.toDomain();
                    User user = chplUserToCognitoUserUtil.getUser(entity.getUserId(), entity.getSsoUserId());
                    qasr.setUser(user);
                    if (user != null) {
                        qasr.setUsername(ObjectUtils.firstNonNull(user.getEmail(), user.getFullName()));
                    }
                    return qasr;
                })
                .collect(Collectors.toList());
    }


}
