package gov.healthit.chpl.report.questionableurl;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class QuestionableUrlReportDao extends BaseDAOImpl {

    public List<QuestionableUrlReport> getQuestionableUrlReports() {
        String hql = "SELECT urlType, count(*) as urlTypeCount "
                + "FROM QuestionableUrlDetailEntity "
                + "GROUP BY urlType";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> QuestionableUrlReport.builder()
                        .urlType((String) result[0])
                        .count((Long) result[1])
                        .build())
                .toList();
    }

    public List<QuestionableUrlDetailReport> getQuestionableUrlDetails() {
        String hql = "SELECT entity "
                + "FROM QuestionableUrlDetailEntity entity ";

        Query query = entityManager.createQuery(hql);
        List<QuestionableUrlDetailEntity> results = query.getResultList();

        return results.stream()
                .map(result -> result.toDomain())
                .toList();
    }
}
