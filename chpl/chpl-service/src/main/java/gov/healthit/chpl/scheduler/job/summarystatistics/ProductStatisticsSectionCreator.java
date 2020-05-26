package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs, FF4j ff4j) {
        return buildUniqueProductSection(stats, new StatisticsMassager(activeAcbs), ff4j);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildUniqueProductSection(Statistics stats, StatisticsMassager massager, FF4j ff4j) {
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

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            List<CertifiedBodyStatistics> productsForAny2015Listing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015ListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings",
                    sumTotalListings(productsForAny2015Listing),
                    productsForAny2015Listing));

            List<CertifiedBodyStatistics> productsForAny2015ActiveListing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015ActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings",
                    sumTotalListings(productsForAny2015ActiveListing),
                    productsForAny2015ActiveListing));

            List<CertifiedBodyStatistics> productsForAny2015SuspendedListing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015SuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings",
                    sumTotalListings(productsForAny2015SuspendedListing),
                    productsForAny2015SuspendedListing));

            List<CertifiedBodyStatistics> productsWithoutCuresUpdatedListing = massager
                    .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with 2015 Listings",
                    sumTotalListings(productsWithoutCuresUpdatedListing),
                    productsWithoutCuresUpdatedListing));

            List<CertifiedBodyStatistics> productsWithoutCuresUpdatedActiveListing = massager
                    .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Active 2015 Listings",
                    sumTotalListings(productsWithoutCuresUpdatedActiveListing),
                    productsWithoutCuresUpdatedActiveListing));

            List<CertifiedBodyStatistics> productsWithoutCuresUpdatedSuspendedListing = massager
                    .getStatistics(stats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                    sumTotalListings(productsWithoutCuresUpdatedSuspendedListing),
                    productsWithoutCuresUpdatedSuspendedListing));

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
        } else {
            List<CertifiedBodyStatistics> productsForAny2015Listing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015ListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with 2015 Listings",
                    sumTotalListings(productsForAny2015Listing),
                    productsForAny2015Listing));

            List<CertifiedBodyStatistics> productsForAny2015ActiveListing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015ActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Active 2015 Listings",
                    sumTotalListings(productsForAny2015ActiveListing),
                    productsForAny2015ActiveListing));

            List<CertifiedBodyStatistics> productsForAny2015SuspendedListing = massager
                    .getStatistics(stats.getUniqueProductsCountForAny2015SuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                    sumTotalListings(productsForAny2015SuspendedListing),
                    productsForAny2015SuspendedListing));
        }

        section.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - ")
        .append(stats.getTotalCPsActiveListings())
        .append("</li></ul>");

        return section.toString();
    }
}
