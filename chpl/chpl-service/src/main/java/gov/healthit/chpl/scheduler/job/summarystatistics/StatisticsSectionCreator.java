package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public abstract class StatisticsSectionCreator {

    public abstract Long getStatistic(CertifiedBodyStatistics stat);

    public List<CertifiedBodyStatistics> getStatisticsByStatusAndEdition(List<CertifiedBodyStatistics> stats,
            String statusName, Integer edition, List<CertificationBodyDTO> activeAcbs) {

        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // Filter the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            if (cbStat.getYear().equals(edition)
                    && cbStat.getCertificationStatusName().toLowerCase().contains(statusName.toLowerCase())) {
                acbStats.add(cbStat);
            }
        }
        addMissingAcbStatistics(acbStats, edition, activeAcbs);
        return acbStats;
    }

    public List<CertifiedBodyStatistics> getStatisticsByEdition(List<CertifiedBodyStatistics> stats,
            Integer edition, List<CertificationBodyDTO> activeAcbs) {

        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // Filter the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            if (cbStat.getYear().equals(edition)) {
                acbStats.add(cbStat);
            }
        }
        addMissingAcbStatistics(acbStats, edition, activeAcbs);
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

    private List<CertifiedBodyStatistics> getMissingAcbStats(List<CertifiedBodyStatistics> statistics,
            Integer edition, List<CertificationBodyDTO> activeAcbs) {

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

    public String buildHeader(String text, Long count) {
        StringBuilder header = new StringBuilder();
        return header.append("<h4>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</h4>")
                .toString();
    }

    public String buildSection(String header, Long headerCount, List<CertifiedBodyStatistics> stats) {
        StringBuilder section = new StringBuilder();

        section.append(buildSectionHeader(header, headerCount))
                .append("<ul>");

        stats.stream()
                .forEach(stat -> section.append(buildAcbCount(stat.getName(), getStatistic(stat))));

        section.append("</ul>");
        return section.toString();
    }

    public String buildItem(String text, Long count) {
        StringBuilder item = new StringBuilder();
        return item.append("<li>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("/<li>")
                .toString();
    }

    private String buildSectionHeader(String header, Long count) {
        StringBuilder section = new StringBuilder();
        return section.append("<li>")
                .append(header)
                .append(" - ")
                .append(count)
                .append("</li>")
                .toString();
    }

    private String buildAcbCount(String acbName, Long count) {
        StringBuilder section = new StringBuilder();
        return section.append("<li>Certified by ")
                .append(acbName)
                .append(" - ")
                .append(count)
                .append("</li>")
                .toString();
    }
}
