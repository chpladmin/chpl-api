package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import lombok.Data;

@Data
public class StatisticsMassager {

    private List<CertificationBody> activeAcbs;

    public StatisticsMassager(List<CertificationBody> activeAcbs) {
        this.activeAcbs = activeAcbs;
    }

    public List<CertificationBodyStatistic> getStatistics(List<CertificationBodyStatistic> stats) {
        List<CertificationBodyStatistic> acbStats = new ArrayList<CertificationBodyStatistic>(stats);
        addMissingAcbStatistics(acbStats);
        acbStats.sort((a, b) -> a.getAcbName().compareTo(b.getAcbName()));
        return acbStats;
    }

    private void addMissingAcbStatistics(List<CertificationBodyStatistic> acbStats) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats));

        acbStats = acbStats.stream()
                .sorted(Comparator.comparing(CertificationBodyStatistic::getAcbName))
                .collect(Collectors.toList());
    }

    private List<CertificationBodyStatistic> getMissingAcbStats(List<CertificationBodyStatistic> statistics) {
        List<CertificationBodyStatistic> updatedStats = activeAcbs.stream()
                .filter(acb -> !isAcbInStatistics(acb, statistics))
                .map(acb -> getNewCertificationBodyStatistic(acb.getName()))
                .collect(Collectors.toList());
        return updatedStats;
    }

    private Boolean isAcbInStatistics(CertificationBody acb, List<CertificationBodyStatistic> stats) {
        return stats.stream()
                .filter(stat -> stat.getAcbName().equals(acb.getName()))
                .findAny()
                .isPresent();
    }

    private CertificationBodyStatistic getNewCertificationBodyStatistic(String acbName) {
        CertificationBodyStatistic stat = new CertificationBodyStatistic();
        stat.setAcbName(acbName);
        stat.setCount(0L);
        return stat;
    }
}
