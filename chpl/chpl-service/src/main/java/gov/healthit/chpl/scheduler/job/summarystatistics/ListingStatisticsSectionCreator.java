package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {
    private static int EDITION2015 = 2015;
    private static int EDITION2014 = 2014;

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildListingSection(stats, activeAcbs);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildListingSection(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Listings (Regardless of Status or Edition)",
                stats.getTotalListings()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)",
                stats.getTotalActive2014Listings(),
                getStatisticsByEdition(stats.getTotalActiveListingsByCertifiedBody(), EDITION2014, activeAcbs)));

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)",
                stats.getTotalActive2015Listings(),
                getStatisticsByEdition(stats.getTotalActiveListingsByCertifiedBody(), EDITION2015, activeAcbs)));

        section.append("<li>Total # of 2015 Listings with Alternative Test Methods -  ")
                .append(stats.getTotalListingsWithAlternativeTestMethods())
                .append("</li>")
                .append("<ul>");
        for (CertifiedBodyAltTestStatistics cbStat : getStatisticsWithAltTestMethods(stats, activeAcbs)) {
            section.append("<li>Certified by ")
                    .append(cbStat.getName())
                    .append(" - ")
                    .append(cbStat.getTotalListings())
                    .append("</li>");
        }
        section.append("</ul>");

        section.append(buildItem("Total # of 2014 Listings (Regardless of Status)", stats.getTotal2014Listings()));
        section.append(buildItem("Total # of 2015 Listings (Regardless of Status)", stats.getTotal2015Listings()));
        section.append(buildItem("Total # of 2011 Listings (Regardless of Status)", stats.getTotal2011Listings()));

        section.append("</ul>");
        return section.toString();
    }

    private List<CertifiedBodyAltTestStatistics> getStatisticsWithAltTestMethods(Statistics stats,
            List<CertificationBodyDTO> activeAcbs) {
        List<CertifiedBodyAltTestStatistics> acbStats = new ArrayList<CertifiedBodyAltTestStatistics>();
        // Filter the existing stats
        for (CertifiedBodyAltTestStatistics cbStat : stats
                .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods()) {

            acbStats.add(cbStat);
        }
        // Add statistics for missing active ACBs
        acbStats.addAll(getMissingAcbWithAltTestMethodsStats(acbStats, activeAcbs));

        Collections.sort(acbStats, new Comparator<CertifiedBodyAltTestStatistics>() {
            public int compare(CertifiedBodyAltTestStatistics obj1, CertifiedBodyAltTestStatistics obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        return acbStats;
    }

    private List<CertifiedBodyAltTestStatistics> getMissingAcbWithAltTestMethodsStats(
            List<CertifiedBodyAltTestStatistics> statistics, List<CertificationBodyDTO> activeAcbs) {

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

}
