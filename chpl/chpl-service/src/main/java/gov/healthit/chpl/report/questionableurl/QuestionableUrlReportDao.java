package gov.healthit.chpl.report.questionableurl;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import jakarta.persistence.Query;

@Repository
public class QuestionableUrlReportDao extends BaseDAOImpl {
    private String unformattedListingDetailsUrl;
    private String unformattedDeveloperDetailsUrl;
    private FF4j ff4j;

    @Autowired
    public QuestionableUrlReportDao(FF4j ff4j,
            @Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.ff4j = ff4j;
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
        this.unformattedDeveloperDetailsUrl = chplUrlBegin + developerUrlPart;
    }

    public List<QuestionableUrlReport> getQuestionableUrlReports() {
        //add all url types to the response
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

        List<QuestionableUrlDetailReport> reports = results.stream()
                .map(result -> result.toDomain())
                .toList();

        //add the URL for any appropriate types
        reports.stream()
            .filter(report -> report.getUrlType().equals(UrlType.DEVELOPER.getName()))
            .forEach(developerReport -> developerReport.setRelatedItemUrl(String.format(unformattedDeveloperDetailsUrl, developerReport.getRelatedItemId())));
        reports.stream()
            .filter(report -> isForListingUrl(report))
            .forEach(listingReport -> listingReport.setRelatedItemUrl(String.format(unformattedListingDetailsUrl, listingReport.getRelatedItemId())));

        return reports;
    }

    private boolean isForListingUrl(QuestionableUrlDetailReport report) {
        return report.getUrlType().equals(UrlType.API_DOCUMENTATION.getName())
                || report.getUrlType().equals(UrlType.DOCUMENTATION.getName())
                || report.getUrlType().equals(UrlType.EXPORT_DOCUMENTATION.getName())
                || (!ff4j.check(FeatureList.HTI_5_ERD) && report.getUrlType().equals(UrlType.FULL_USABILITY_REPORT.getName()))
                || report.getUrlType().equals(UrlType.MANDATORY_DISCLOSURE.getName())
                || (!ff4j.check(FeatureList.HTI_5_ERD) && report.getUrlType().equals(UrlType.REAL_WORLD_TESTING_PLANS.getName()))
                || report.getUrlType().equals(UrlType.REAL_WORLD_TESTING_RESULTS.getName())
                || report.getUrlType().equals(UrlType.RISK_MANAGEMENT_SUMMARY_INFORMATION.getName())
                || report.getUrlType().equals(UrlType.SERVICE_BASE_URL_LIST.getName())
                || report.getUrlType().equals(UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE.getName())
                || report.getUrlType().equals(UrlType.USE_CASES.getName());
    }
}
