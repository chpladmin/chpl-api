package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.statistics.AcbStat;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import lombok.Data;

@Data
public class StatisticsMassager {

    private List<CertificationBodyDTO> activeAcbs;

    public StatisticsMassager(List<CertificationBodyDTO> activeAcbs) {
        this.activeAcbs = activeAcbs;
    }

    public List<AcbStat> getStatistics(List<AcbStat> stats) {
        List<AcbStat> acbStats = new ArrayList<AcbStat>(stats);
        addMissingAcbStatistics(acbStats);
        acbStats.sort((a, b) -> a.getAcbName().compareTo(b.getAcbName()));
        return acbStats;
    }

    private void addMissingAcbStatistics(List<AcbStat> acbStats) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats));

        acbStats = acbStats.stream()
                .sorted(Comparator.comparing(AcbStat::getAcbName))
                .collect(Collectors.toList());
    }

    private List<AcbStat> getMissingAcbStats(List<AcbStat> statistics) {
        List<AcbStat> updatedStats = activeAcbs.stream()
                .filter(acb -> !isAcbInStatistics(acb, statistics))
                .map(acb -> getNewCertifiedBodyStatistic(acb.getName()))
                .collect(Collectors.toList());
        return updatedStats;
    }

    private Boolean isAcbInStatistics(CertificationBodyDTO acb, List<AcbStat> stats) {
        return stats.stream()
                .filter(stat -> stat.getAcbName().equals(acb.getName()))
                .findAny()
                .isPresent();
    }

    private AcbStat getNewCertifiedBodyStatistic(String acbName) {
        AcbStat stat = new AcbStat();
        stat.setAcbName(acbName);
        stat.setCount(0L);
        return stat;
    }
}
