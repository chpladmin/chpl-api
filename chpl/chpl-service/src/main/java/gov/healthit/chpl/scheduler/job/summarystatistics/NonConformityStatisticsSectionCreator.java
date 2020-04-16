package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.Map.Entry;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class NonConformityStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildNonConformitySection(stats, new StatisticsMassager(activeAcbs));
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildNonConformitySection(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of NCs",
                stats.getTotalNonConformities()));

        section.append("<ul>");

        section.append(buildSection(
                "Open NCs",
                stats.getTotalOpenNonconformities(),
                massager.getStatistics(stats.getTotalOpenNonconformitiesByAcb())));

        section.append(buildItem("Average Time to Assess Conformity (in days)", stats.getAverageTimeToAssessConformity()));
        section.append(buildItem("Average Time to Approve CAP (in days)", stats.getAverageTimeToApproveCAP()));
        section.append(buildItem("Average Duration of CAP (in days) (includes closed and ongoing CAPs)",
                stats.getAverageDurationOfCAP()));
        section.append(buildItem("Average Time from CAP Approval to Surveillance Close (in days)",
                stats.getAverageTimeFromCAPApprovalToSurveillanceEnd()));
        section.append(buildItem("Average Time from CAP Close to Surveillance Close (in days)",
                stats.getAverageTimeFromCAPEndToSurveillanceEnd()));
        section.append(buildItem("Average Duration of Closed Non-Conformities (in days)",
                stats.getAverageTimeFromSurveillanceOpenToSurveillanceClose()));

        section.append(buildOpenNumberOfCaps(stats, massager));
        section.append(buildClosedNumberOfCaps(stats, massager));

        section.append("</ul>");
        return section.toString();
    }

    private String buildOpenNumberOfCaps(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append("<li>Number of Open CAPs</li>");
        section.append("<ul>");
        for (Entry<Long, Long> entry : stats.getOpenCAPCountByAcb().entrySet()) {
            CertificationBodyDTO acb = getAcb(massager.getActiveAcbs(), entry.getKey());
            section.append("<li>Certified by ");
            section.append(acb.getName());
            section.append(" - ");
            section.append(entry.getValue());
            section.append("</li>");
        }
        section.append("</ul>");
        return section.toString();
    }

    private String buildClosedNumberOfCaps(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();
        section.append("<li>Number of Closed CAPs</li>");
        section.append("<ul>");
        for (Entry<Long, Long> entry : stats.getClosedCAPCountByAcb().entrySet()) {
            CertificationBodyDTO acb = getAcb(massager.getActiveAcbs(), entry.getKey());
            section.append("<li>Certified by ");
            section.append(acb.getName());
            section.append(" - ");
            section.append(entry.getValue());
            section.append("</li>");
        }
        section.append("</ul>");
        return section.toString();
    }

    private CertificationBodyDTO getAcb(List<CertificationBodyDTO> acbs, Long acbId) {
        return acbs.stream()
                .filter(acb -> acb.getId().equals(acbId))
                .findAny()
                .get();

    }
}
