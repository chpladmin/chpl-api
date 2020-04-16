package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class SurveillanceStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildSurveillanceSection(stats, activeAcbs);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalDevelopersWithListings();
    }

    private String buildSurveillanceSection(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Surveillance Activities", stats.getTotalSurveillanceActivities()));
        section.append("<ul>");

        section.append(buildSection(
                "Open Surveillance Activities",
                stats.getTotalOpenSurveillanceActivities(),
                getStatistics(stats.getTotalOpenSurveillanceActivitiesByAcb(), activeAcbs)));

        section.append("<li>Closed Surveillance Activities - ")
                .append(stats.getTotalClosedSurveillanceActivities())
                .append("</li>");

        section.append("<li>Average Duration of Closed Surveillance (in days) - ")
                .append(stats.getAverageTimeToCloseSurveillance())
                .append("</li>");

        section.append("</ul>");
        return section.toString();
    }

    private List<CertifiedBodyStatistics> getStatistics(List<CertifiedBodyStatistics> stats,
            List<CertificationBodyDTO> activeAcbs) {
        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // All the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            acbStats.add(cbStat);
        }
        addMissingAcbStatistics(acbStats, null, activeAcbs);
        return acbStats;
    }

    private void addMissingAcbStatistics(List<CertifiedBodyStatistics> acbStats, Integer edition,
            List<CertificationBodyDTO> activeAcbs) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats, edition, activeAcbs));

        Collections.sort(acbStats, new Comparator<CertifiedBodyStatistics>() {
            public int compare(CertifiedBodyStatistics obj1, CertifiedBodyStatistics obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
    }

    private List<CertifiedBodyStatistics> getMissingAcbStats(List<CertifiedBodyStatistics> statistics, Integer edition,
            List<CertificationBodyDTO> activeAcbs) {

        List<CertifiedBodyStatistics> updatedStats = new ArrayList<CertifiedBodyStatistics>();
        // Make sure all active ACBs are in the resultset
        for (CertificationBodyDTO acb : activeAcbs) {
            if (!isAcbInStatistics(acb, statistics)) {
                updatedStats.add(getNewCertifiedBodyStatistic(acb.getName(), edition));
            }
        }
        return updatedStats;
    }

    private Boolean isAcbInStatistics(CertificationBodyDTO acb, List<CertifiedBodyStatistics> stats) {
        for (CertifiedBodyStatistics stat : stats) {
            if (stat.getName().equals(acb.getName())) {
                return true;
            }
        }
        return false;
    }

    private CertifiedBodyStatistics getNewCertifiedBodyStatistic(String acbName, Integer year) {
        CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
        stat.setName(acbName);
        stat.setTotalDevelopersWithListings(0L);
        stat.setTotalListings(0L);
        stat.setYear(year);
        return stat;
    }

}
