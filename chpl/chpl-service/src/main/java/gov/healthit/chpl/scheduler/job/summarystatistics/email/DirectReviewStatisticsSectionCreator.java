package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

public class DirectReviewStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(EmailStatistics stats) {
        return buildDirectReviewSection(stats);
    }

    private String buildDirectReviewSection(EmailStatistics stats) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Direct Review Activities", handleNullValue(stats.getTotalDirectReviews())));
        section.append("<ul>");
        section.append(buildItem("Open Direct Review Activities", handleNullValue(stats.getOpenDirectReviews())));
        section.append(buildItem("Closed Direct Review Activities", handleNullValue(stats.getClosedDirectReviews())));
        section.append(buildItem("Average Duration of Closed Direct Review Activities (in days)", handleNullValue(stats.getAverageDaysOpenDirectReviews())));
        section.append("</ul>");

        section.append(buildHeader("Total # of Direct Review NCs", handleNullValue(stats.getTotalNonConformities())));
        section.append("<ul>");
        section.append(buildItem("Open Direct Review NCs", handleNullValue(stats.getOpenNonConformities())));
        section.append(buildItem("Closed Direct Review NCs", handleNullValue(stats.getClosedNonConformities())));
        section.append(buildItem("Number of Open CAPs", handleNullValue(stats.getOpenCaps())));
        section.append(buildItem("Number of Closed CAPs", handleNullValue(stats.getClosedCaps())));
        section.append("</ul>");

        return section.toString();
    }

    private String handleNullValue(Long value) {
        return value != null ? value.toString() : "Not Available";
    }
}
