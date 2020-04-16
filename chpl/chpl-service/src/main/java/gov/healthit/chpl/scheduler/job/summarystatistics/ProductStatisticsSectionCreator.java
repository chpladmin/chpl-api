package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {
    private static int EDITION2015 = 2015;
    private static int EDITION2014 = 2014;

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueProductSection(stats, activeAcbs);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildUniqueProductSection(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)",
                stats.getTotalCertifiedProducts()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Unique Products with 2014 Listings (Regardless of Status)",
                stats.getTotalCPs2014Listings(),
                getStatisticsByEdition(stats.getTotalCPListingsEachYearByCertifiedBody(), EDITION2014, activeAcbs)));

        section.append(buildSection(
                "Total # of Unique Products with Active 2014 Listings",
                stats.getTotalCPsActive2014Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), "Active",
                        EDITION2014, activeAcbs)));

        section.append(buildSection(
                "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                stats.getTotalCPsSuspended2014Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Suspended", EDITION2014, activeAcbs)));

        section.append(buildSection(
                "Total # of Unique Products with 2015 Listings (Regardless of Status)",
                stats.getTotalCPs2015Listings(),
                getStatisticsByEdition(stats.getTotalCPListingsEachYearByCertifiedBody(), EDITION2015, activeAcbs)));

        section.append(buildSection(
                "Total # of Unique Products with Active 2015 Listings",
                stats.getTotalCPsActive2015Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), "Active",
                        EDITION2015, activeAcbs)));

        section.append(buildSection(
                "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                stats.getTotalCPsSuspended2015Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Suspended", EDITION2015, activeAcbs)));

        section.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - ")
                .append(stats.getTotalCPsActiveListings())
                .append("</ul></li>");

        return section.toString();
    }

}
