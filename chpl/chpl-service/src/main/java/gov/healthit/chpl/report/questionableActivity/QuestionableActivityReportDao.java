package gov.healthit.chpl.report.questionableActivity;

import java.util.List;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivitySearchResultEntity;
import gov.healthit.chpl.questionableactivity.search.QuestionableActivitySearchResult;
import jakarta.persistence.Query;

@Repository
public class QuestionableActivityReportDao extends BaseDAOImpl {
    private static final int REPORT_MONTHS = 6;

    private String unformattedListingDetailsUrl;
    private String unformattedDeveloperDetailsUrl;
    private FF4j ff4j;

    @Autowired
    public QuestionableActivityReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
        this.unformattedDeveloperDetailsUrl = chplUrlBegin + developerUrlPart;
    }

    public List<QuestionableActivityReport> getQuestionableActivityReports() {
        Query query = entityManager.createQuery("SELECT qa "
                + "FROM QuestionableActivitySearchResultEntity qa "
                + "WHERE qa.activityDate >= :activityDate ",
                QuestionableActivitySearchResultEntity.class);
        query.setParameter("activityDate", LocalDateTime.now().minusMonths(REPORT_MONTHS).toDate());

        List<QuestionableActivitySearchResultEntity> queryResults = query.getResultList();
        List<QuestionableActivityReport> results = queryResults.stream()
                .map(entity -> entity.toDomain())
                .map(activity -> QuestionableActivityReport.builder()
                        .activityType(activity.getTriggerName())
                        .activityDate(activity.getActivityDate().toLocalDate())
                        .developer(activity.getDeveloperName())
                        .relatedItem(getRelatedItemName(activity))
                        .relatedItemId(getRelatedItemId(activity))
                        .relatedItemUrl(getRelatedItemUrl(activity))
                        .build())
                .collect(Collectors.toList());
        return results;
    }

    private String getRelatedItemName(QuestionableActivitySearchResult activity) {
        if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_LISTING)
                || activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_CRITERIA)) {
            return activity.getChplProductNumber();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_DEVELOPER)) {
            return activity.getDeveloperName();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_PRODUCT)) {
            return activity.getProductName();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_VERSION)) {
            return activity.getVersionName();
        }
        return activity.getDescription();
    }

    private Long getRelatedItemId(QuestionableActivitySearchResult activity) {
        if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_LISTING)
                || activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_CRITERIA)) {
            return activity.getListingId();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_DEVELOPER)) {
            return activity.getDeveloperId();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_PRODUCT)) {
            return activity.getProductId();
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_VERSION)) {
            return activity.getVersionId();
        }
        return null;
    }

    private String getRelatedItemUrl(QuestionableActivitySearchResult activity) {
        if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_LISTING)
                || activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_CRITERIA)) {
            return String.format(unformattedListingDetailsUrl, activity.getListingId());
        } else if (activity.getTriggerLevel().equals(QuestionableActivityTriggerConcept.LEVEL_DEVELOPER)) {
            return String.format(unformattedDeveloperDetailsUrl, activity.getDeveloperId());
        }
        return null;
    }
}
