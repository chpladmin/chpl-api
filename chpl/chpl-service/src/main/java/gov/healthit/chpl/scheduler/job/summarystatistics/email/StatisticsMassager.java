package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;
import lombok.Data;

@Data
public class StatisticsMassager {

    private List<CertificationBodyDTO> activeAcbs;

    public StatisticsMassager(List<CertificationBodyDTO> activeAcbs) {
        this.activeAcbs = activeAcbs;
    }

    public List<EmailCertificationBodyStatistic> getStatistics(List<EmailCertificationBodyStatistic> stats) {
        List<EmailCertificationBodyStatistic> acbStats = new ArrayList<EmailCertificationBodyStatistic>(stats);
        addMissingAcbStatistics(acbStats);
        acbStats.sort((a, b) -> a.getAcbName().compareTo(b.getAcbName()));
        return acbStats;
    }

    private void addMissingAcbStatistics(List<EmailCertificationBodyStatistic> acbStats) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats));

        acbStats = acbStats.stream()
                .sorted(Comparator.comparing(EmailCertificationBodyStatistic::getAcbName))
                .collect(Collectors.toList());
    }

    private List<EmailCertificationBodyStatistic> getMissingAcbStats(List<EmailCertificationBodyStatistic> statistics) {
        List<EmailCertificationBodyStatistic> updatedStats = activeAcbs.stream()
                .filter(acb -> !isAcbInStatistics(acb, statistics))
                .map(acb -> getNewCertificationBodyStatistic(acb.getName()))
                .collect(Collectors.toList());
        return updatedStats;
    }

    private Boolean isAcbInStatistics(CertificationBodyDTO acb, List<EmailCertificationBodyStatistic> stats) {
        return stats.stream()
                .filter(stat -> stat.getAcbName().equals(acb.getName()))
                .findAny()
                .isPresent();
    }

    private EmailCertificationBodyStatistic getNewCertificationBodyStatistic(String acbName) {
        EmailCertificationBodyStatistic stat = new EmailCertificationBodyStatistic();
        stat.setAcbName(acbName);
        stat.setCount(0L);
        return stat;
    }
}
