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

        section.append(buildSection(
                "Total # of Developers with 2015 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2015Listings(),
                massager.getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(),
                        get2015EditionAsInteger())));

        section.append(buildSection(
                "Total # of Developers with Active 2015 Listings",
                stats.getTotalDevelopersWithActive2015Listings(),
                massager.getStatisticsByStatusAndEdition(
                        stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", get2015EditionAsInteger())));

        List<CertifiedBodyStatistics> devsSuspended2015 = massager.getStatisticsByStatusAndEdition(
                stats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                "Suspended", get2015EditionAsInteger());
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                sumTotalDeveloperWithListings(devsSuspended2015),
                devsSuspended2015));

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            List<CertifiedBodyStatistics> devsWithCuresUpdatedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with 2015-Cures Update Listings (Regardless of Status)",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedListing),
                    devsWithCuresUpdatedListing));

            List<CertifiedBodyStatistics> devsWithCuresUpdatedActiveListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Active 2015-Cures Update Listing",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedActiveListing),
                    devsWithCuresUpdatedActiveListing));

            List<CertifiedBodyStatistics> devsWithCuresUpdatedSuspendedListing = massager
                    .getStatistics(stats.getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb());
            section.append(buildSection(
                    "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings",
                    sumTotalDeveloperWithListings(devsWithCuresUpdatedSuspendedListing),
                    devsWithCuresUpdatedSuspendedListing));
        }
        section.append("</ul>");
        return section.toString();
    }
}
