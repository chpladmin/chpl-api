package gov.healthit.chpl.report.questionableurl;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlTypeEntity;
import jakarta.persistence.Query;

@Repository
public class QuestionableUrlReportDao extends BaseDAOImpl {

    public List<QuestionableUrlReport> getQuestionableUrlReports() {
        //add all url types to the response
        String hql = "SELECT urlType FROM UrlTypeEntity urlType WHERE deleted = false";
        Query query = entityManager.createQuery(hql);
        List<UrlTypeEntity> allUrlTypes = query.getResultList();
        List<QuestionableUrlReport> response = allUrlTypes.stream()
                .map(urlType -> QuestionableUrlReport.builder()
                        .urlType(urlType.getName())
                        .count(0L)
                        .build())
                .toList();

        //update the counts for url types that are nonzero
        hql = "SELECT urlType, count(*) as urlTypeCount "
                + "FROM QuestionableUrlDetailEntity "
                + "GROUP BY urlType";

        query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();
        results.stream()
            .forEach(result -> updateCount(response, (String) result[0], (Long) result[1]));
        return response;
    }

    private void updateCount(List<QuestionableUrlReport> reports, String urlTypeName, Long count) {
        QuestionableUrlReport reportForUrlType = reports.stream()
            .filter(report -> report.getUrlType().equals(urlTypeName))
            .findAny()
            .orElse(null);
        if (reportForUrlType != null) {
            reportForUrlType.setCount(count);
        }
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
