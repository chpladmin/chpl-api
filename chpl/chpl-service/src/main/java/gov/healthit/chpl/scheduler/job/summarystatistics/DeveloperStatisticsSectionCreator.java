package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class DeveloperStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs, FF4j ff4j) {
        return buildUniqueDeveloperSection(stats, new StatisticsMassager(activeAcbs), ff4j);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalDevelopersWithListings();
    }

    private String buildUniqueDeveloperSection(Statistics stats, StatisticsMassager massager, FF4j ff4j) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Unique Developers (Regardless of Edition)", stats.getTotalDevelopers()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Developers with 2014 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2014Listings(),
                massager.getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(),
                        get2014EditionAsInteger())));
        section.append(buildSection(
                "Total # of Developers with Active 2014 Listings",
                stats.getTotalDevelopersWithActive2014Listings(),
                massager.getStatisticsByStatusAndEdition(
                        stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", get2014EditionAsInteger())));

        List<CertifiedBodyStatistics> devsSuspended2014 = massager.getStatisticsByStatusAndEdition(
                stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), "Suspended",
                get2014EditionAsInteger());
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                sumTotalDeveloperWithListings(devsSuspended2014),
                devsSuspended2014));

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            List<CertifiedBodyStatistics> devsWith2015Listing = massager
                    .getStatistics(stats.getUniqueDevelopersCountForAny2015ListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)",
                    sumTotalDeveloperWithListings(devsWith2015Listing),
                    devsWith2015Listing));

            List<CertifiedBodyStatistics> devsForAll2015ActiveListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountForAny2015ActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings",
                    sumTotalDeveloperWithListings(devsForAll2015ActiveListing),
                    devsForAll2015ActiveListing));

            List<CertifiedBodyStatistics> devsWithForAllSuspendedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings",
                    sumTotalDeveloperWithListings(devsWithForAllSuspendedListing),
                    devsWithForAllSuspendedListing));

            List<CertifiedBodyStatistics> devsWithoutCuresUpdatedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithoutCuresUpdatedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with 2015 Listings (Regardless of Status)",
                    sumTotalDeveloperWithListings(devsWithoutCuresUpdatedListing),
                    devsWithoutCuresUpdatedListing));

            List<CertifiedBodyStatistics> devsWithoutCuresUpdatedActiveListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithoutCuresUpdatedActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Active 2015 Listings",
                    sumTotalDeveloperWithListings(devsWithoutCuresUpdatedActiveListing),
                    devsWithoutCuresUpdatedActiveListing));

            List<CertifiedBodyStatistics> devsWithoutCuresUpdatedSuspendedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                    sumTotalDeveloperWithListings(devsWithoutCuresUpdatedSuspendedListing),
                    devsWithoutCuresUpdatedSuspendedListing));

            List<CertifiedBodyStatistics> devsWithCuresUpdatedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with 2015 Cures Update Listings (Regardless of Status)",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedListing),
                    devsWithCuresUpdatedListing));

            List<CertifiedBodyStatistics> devsWithCuresUpdatedActiveListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Active 2015 Cures Update Listings",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedActiveListing),
                    devsWithCuresUpdatedActiveListing));

            List<CertifiedBodyStatistics> devsWithCuresUpdatedSuspendedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedSuspendedListing),
                    devsWithCuresUpdatedSuspendedListing));
        } else {
            List<CertifiedBodyStatistics> devsWith2015Listing = massager
                    .getStatistics(stats.getUniqueDevelopersCountForAny2015ListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with 2015 Listings (Regardless of Status)",
                    sumTotalDeveloperWithListings(devsWith2015Listing),
                    devsWith2015Listing));

            List<CertifiedBodyStatistics> devsForAll2015ActiveListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountForAny2015ActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Active 2015 Listings",
                    sumTotalDeveloperWithListings(devsForAll2015ActiveListing),
                    devsForAll2015ActiveListing));

            List<CertifiedBodyStatistics> devsWithForAllSuspendedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                    sumTotalDeveloperWithListings(devsWithForAllSuspendedListing),
                    devsWithForAllSuspendedListing));

        }
        section.append("</ul>");
        return section.toString();
    }
}
