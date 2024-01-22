package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.StatisticsMassager;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;

public class NonConformityStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(StatisticsSnapshot stats, List<CertificationBody> activeAcbs) {
        return buildNonConformitySection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildNonConformitySection(StatisticsSnapshot stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Surveillance NCs (2015 Edition to Present)", stats.getNonConfStatusAllTotal()));
        section.append("<ul>");

        section.append(buildSection(
                "Open Surveillance NCs",
                stats.getNonConfStatusOpen().getCount(),
                massager.getStatistics(stats.getNonConfStatusOpen().getAcbStatistics())));

        section.append(buildItem("Closed Surveillance NCs", stats.getNonConfStatusClosedTotal()));
        section.append(buildItem("Average Time to Assess Conformity (in days)", stats.getNonConfAvgTimeToAssessConformity()));
        section.append(buildItem("Average Time to Approve CAP (in days)", stats.getNonConfAvgTimeToApproveCAP()));
        section.append(buildItem("Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                stats.getNonConfAvgDurationOfCAP()));
        section.append(buildItem("Average Time from CAP Approval to Surveillance Close (in days)",
                stats.getNonConfAvgTimeFromCAPAprrovalToSurveillanceEnd()));
        section.append(buildItem("Average Time from CAP Close to Surveillance Close (in days)",
                stats.getNonConfAvgTimeFromCAPEndToSurveillanceEnd()));
        section.append(buildItem("Average Duration of Closed Non-Conformities (in days)",
                stats.getNonConfAvgTimeFromSurveillanceOpenToSurveillanceClose()));

        section.append(buildSection(
                "Number of Open CAPs",
                null,
                massager.getStatistics(stats.getNonConfCAPStatusOpen())));

        section.append(buildSection(
                "Number of Closed CAPs",
                null,
                massager.getStatistics(stats.getNonConfCAPStatusClosed())));

        section.append("</ul>");
        return section.toString();
    }
}
