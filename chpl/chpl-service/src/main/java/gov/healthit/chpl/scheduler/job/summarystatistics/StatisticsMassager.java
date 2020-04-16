package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import lombok.Data;

@Data
public class StatisticsMassager {

    private List<CertificationBodyDTO> activeAcbs;

    public StatisticsMassager(List<CertificationBodyDTO> activeAcbs) {
        this.activeAcbs = activeAcbs;
    }

    public List<CertifiedBodyStatistics> getStatisticsByStatusAndEdition(List<CertifiedBodyStatistics> stats,
            String statusName, Integer edition) {

        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // Filter the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            if (cbStat.getYear().equals(edition)
                    && cbStat.getCertificationStatusName().toLowerCase().contains(statusName.toLowerCase())) {
                acbStats.add(cbStat);
            }
        }
        addMissingAcbStatistics(acbStats, edition);
        return acbStats;
    }

    public List<CertifiedBodyStatistics> getStatisticsByEdition(List<CertifiedBodyStatistics> stats, Integer edition) {

        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // Filter the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            if (cbStat.getYear().equals(edition)) {
                acbStats.add(cbStat);
            }
        }
        addMissingAcbStatistics(acbStats, edition);
        return acbStats;
    }

    private void addMissingAcbStatistics(List<CertifiedBodyStatistics> acbStats, Integer edition) {
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbStats(acbStats, edition));

        Collections.sort(acbStats, new Comparator<CertifiedBodyStatistics>() {
            public int compare(CertifiedBodyStatistics obj1, CertifiedBodyStatistics obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });
    }

    private List<CertifiedBodyStatistics> getMissingAcbStats(List<CertifiedBodyStatistics> statistics,
            Integer edition) {

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

    public List<CertifiedBodyAltTestStatistics> getStatisticsWithAltTestMethods(Statistics stats) {
        List<CertifiedBodyAltTestStatistics> acbStats = new ArrayList<CertifiedBodyAltTestStatistics>();
        // Filter the existing stats
        for (CertifiedBodyAltTestStatistics cbStat : stats
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods()) {

            acbStats.add(cbStat);
        }
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbWithAltTestMethodsStats(acbStats));

        Collections.sort(acbStats, new Comparator<CertifiedBodyAltTestStatistics>() {
            public int compare(CertifiedBodyAltTestStatistics obj1, CertifiedBodyAltTestStatistics obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        return acbStats;
    }

    private List<CertifiedBodyAltTestStatistics> getMissingAcbWithAltTestMethodsStats(
            List<CertifiedBodyAltTestStatistics> statistics) {

        List<CertifiedBodyAltTestStatistics> updatedStats = new ArrayList<CertifiedBodyAltTestStatistics>();
        // Make sure all active ACBs are in the resultset
        for (CertificationBodyDTO acb : activeAcbs) {
            if (!isAcbWithAltTestMethodsInStatistics(acb, statistics)) {
                updatedStats.add(getNewCertifiedBodyWithAltTestMethodsStatistic(acb.getName()));
            }
        }
        return updatedStats;
    }

    private CertifiedBodyAltTestStatistics getNewCertifiedBodyWithAltTestMethodsStatistic(String acbName) {
        CertifiedBodyAltTestStatistics stat = new CertifiedBodyAltTestStatistics();
        stat.setName(acbName);
        stat.setTotalDevelopersWithListings(0L);
        stat.setTotalListings(0L);
        return stat;
    }

    private Boolean isAcbWithAltTestMethodsInStatistics(CertificationBodyDTO acb,
            List<CertifiedBodyAltTestStatistics> stats) {

        for (CertifiedBodyAltTestStatistics stat : stats) {
            if (stat.getName().equals(acb.getName())) {
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////
    public List<CertifiedBodyStatistics> getStatistics(List<CertifiedBodyStatistics> stats) {
        List<CertifiedBodyStatistics> acbStats = new ArrayList<CertifiedBodyStatistics>();
        // All the existing stats
        for (CertifiedBodyStatistics cbStat : stats) {
            acbStats.add(cbStat);
        }
        addMissingAcbStatistics(acbStats, null);
        return acbStats;
    }

}
