package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class SurveillanceStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildSurveillanceSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildSurveillanceSection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Surveillance Activities (2015 Edition to Present)", stats.getSurveillanceAllStatusTotal()));
        section.append("<ul>");

        section.append(buildSection(
                "Open Surveillance Activities",
                stats.getSurveillanceOpenStatus().getCount(),
                massager.getStatistics(stats.getSurveillanceOpenStatus().getAcbStatistics())));

        section.append(buildItem("Closed Surveillance Activities", stats.getSurveillanceClosedStatusTotal()));
        section.append(buildItem(
                "Average Duration of Closed Surveillance (in days)", stats.getSurveillanceAvgTimeToClose()));

        section.append("</ul>");
        return section.toString();
    }
}
