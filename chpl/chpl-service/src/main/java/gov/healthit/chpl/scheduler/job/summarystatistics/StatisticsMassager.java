package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import lombok.Data;

@Data
public class StatisticsMassager {

    private List<CertificationBodyDTO> activeAcbs;

    public StatisticsMassager(List<CertificationBodyDTO> activeAcbs) {
        this.activeAcbs = activeAcbs;
    }

    public List<CertifiedBodyStatistics> getStatistics(List<CertifiedBodyStatistics> stats) {
        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>(stats);
        addMissingAcbStatistics(acbStats, null);
        return acbStats;
    }

    public List<CertifiedBodyStatistics> getStatisticsByStatusAndEdition(List<CertifiedBodyStatistics> stats,
            String statusName, Integer edition) {

        List<CertifiedBodyStatistics> acbStats = stats.stream()
                .filter(stat -> stat.getYear().equals(edition)
                        && stat.getCertificationStatusName().toLowerCase().contains(statusName.toLowerCase()))
                .collect(Collectors.toList());

        addMissingAcbStatistics(acbStats, edition);
        return acbStats;
    }

    public List<CertifiedBodyStatistics> getStatisticsByEdition(List<CertifiedBodyStatistics> stats, Integer edition) {
        List<CertifiedBodyStatistics> acbStats = stats.stream()
                .filter(stat -> stat.getYear().equals(edition))
                .collect(Collectors.toList());
        addMissingAcbStatistics(acbStats, edition);
        return acbStats;
    }

    private void addMissingAcbStatistics(List<CertifiedBodyStatistics> acbStats, Integer edition) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats, edition));

        acbStats = acbStats.stream()
                .sorted(Comparator.comparing(CertifiedBodyStatistics::getName))
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getMissingAcbStats(List<CertifiedBodyStatistics> statistics,
            Integer edition) {

        List<CertifiedBodyStatistics> updatedStats = activeAcbs.stream()
                .filter(acb -> !isAcbInStatistics(acb, statistics))
                .map(acb -> getNewCertifiedBodyStatistic(acb.getName(), edition))
                .collect(Collectors.toList());
        return updatedStats;
    }

    private Boolean isAcbInStatistics(CertificationBodyDTO acb, List<CertifiedBodyStatistics> stats) {
        return stats.stream()
                .filter(stat -> stat.getName().equals(acb.getName()))
                .findAny()
                .isPresent();
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
