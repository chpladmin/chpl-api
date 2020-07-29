package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailStatistics;

public class NonConformityStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(EmailStatistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildNonConformitySection(stats, new StatisticsMassager(activeAcbs));
    }


    private String buildNonConformitySection(EmailStatistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of NCs", stats.getNonconfStatusAllTotal()));
        section.append("<ul>");

        section.append(buildSection(
                "Open NCs",
                stats.getNonconfStatusOpen().getCount(),
                massager.getStatistics(stats.getNonconfStatusOpen().getAcbStatistics())));

        section.append(buildItem("Closed NCs", stats.getNonconfStatusClosedTotal()));
        section.append(buildItem("Average Time to Assess Conformity (in days)", stats.getNonconfAvgTimeToAssessConformity()));
        section.append(buildItem("Average Time to Approve CAP (in days)", stats.getNonconfAvgTimeToApproveCAP()));
        section.append(buildItem("Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                stats.getNonconfAvgDurationOfCAP()));
        section.append(buildItem("Average Time from CAP Approval to Surveillance Close (in days)",
                stats.getNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd()));
        section.append(buildItem("Average Time from CAP Close to Surveillance Close (in days)",
                stats.getNonconfAvgTimeFromCAPEndToSurveillanceEnd()));
        section.append(buildItem("Average Duration of Closed Non-Conformities (in days)",
                stats.getNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose()));

        section.append(buildSection(
                "Number of Open CAPs",
                null,
                massager.getStatistics(stats.getNonconfCAPStatusOpen())));

        section.append(buildSection(
                "Number of Closed CAPs",
                null,
                massager.getStatistics(stats.getNonconfCAPStatusClosed())));

        section.append("</ul>");
        return section.toString();
    }
}
