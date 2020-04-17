package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueProductSection(stats, new StatisticsMassager(activeAcbs));
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildUniqueProductSection(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)",
                stats.getTotalCertifiedProducts()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Unique Products with 2014 Listings (Regardless of Status)",
                stats.getTotalCPs2014Listings(),
                massager.getStatisticsByEdition(stats.getTotalCPListingsEachYearByCertifiedBody(), get2014EditionAsInteger())));

        section.append(buildSection(
                "Total # of Unique Products with Active 2014 Listings",
                stats.getTotalCPsActive2014Listings(),
                massager.getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Active",
                        get2014EditionAsInteger())));

        section.append(buildSection(
                "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                stats.getTotalCPsSuspended2014Listings(),
                massager.getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Suspended", get2014EditionAsInteger())));

        section.append(buildSection(
                "Total # of Unique Products with 2015 Listings (Regardless of Status)",
                stats.getTotalCPs2015Listings(),
                massager.getStatisticsByEdition(stats.getTotalCPListingsEachYearByCertifiedBody(), get2015EditionAsInteger())));

        section.append(buildSection(
                "Total # of Unique Products with Active 2015 Listings",
                stats.getTotalCPsActive2015Listings(),
                massager.getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Active",
                        get2015EditionAsInteger())));

        section.append(buildSection(
                "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                stats.getTotalCPsSuspended2015Listings(),
                massager.getStatisticsByStatusAndEdition(stats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(),
                        "Suspended", get2015EditionAsInteger())));

        List<CertifiedBodyStatistics> productsWithCuresUpdatedListing = massager
                .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedListingsByAcb());
        section.append(buildSection(
                "Total # of Unique Products with 2015-Cures Update Listings",
                sumTotalListings(productsWithCuresUpdatedListing),
                productsWithCuresUpdatedListing));

        List<CertifiedBodyStatistics> productsWithCuresUpdatedActiveListing = massager
                .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb());
        section.append(buildSection(
                "Total # of Unique Products with Active 2015-Cures Update Listings",
                sumTotalListings(productsWithCuresUpdatedActiveListing),
                productsWithCuresUpdatedActiveListing));

        List<CertifiedBodyStatistics> productsWithCuresUpdatedSuspendedListing = massager
                .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb());
        section.append(buildSection(
                "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings",
                sumTotalListings(productsWithCuresUpdatedSuspendedListing),
                productsWithCuresUpdatedSuspendedListing));

        section.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - ")
                .append(stats.getTotalCPsActiveListings())
                .append("</li></ul>");

        return section.toString();
    }
}
