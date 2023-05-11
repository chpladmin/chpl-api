package gov.healthit.chpl.questionableactivity.search;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityEntity;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("questionableActivitySearchDao")
public class QuestionableActivitySearchDAO extends BaseDAOImpl {

    @Transactional
    public List<QuestionableActivity> getAll() {
        Query query = entityManager.createQuery("SELECT qa FROM QuestionableActivityEntity qa ",
                QuestionableActivityEntity.class);
        List<QuestionableActivityEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }
}
