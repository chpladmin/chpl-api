package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class DeveloperStatisticsSectionCreator extends StatisticsSectionCreator {
    private static int EDITION_2015 = 2015;
    private static int EDITION_2014 = 2014;

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueDeveloperSection(stats, new StatisticsMassager(activeAcbs));
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalDevelopersWithListings();
    }

    private String buildUniqueDeveloperSection(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Unique Developers (Regardless of Edition)", stats.getTotalDevelopers()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Developers with 2014 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2014Listings(),
                massager.getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), EDITION_2014)));

        section.append(buildSection(
                "Total # of Developers with Active 2014 Listings",
                stats.getTotalDevelopersWithActive2014Listings(),
                massager.getStatisticsByStatusAndEdition(
                        stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", EDITION_2014)));

        List<CertifiedBodyStatistics> devsSuspended2014 = massager.getStatisticsByStatusAndEdition(
                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), "Suspended", EDITION_2014);
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                sumTotalDeveloperWithListings(devsSuspended2014),
                devsSuspended2014));

        section.append(buildSection(
                "Total # of Developers with 2015 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2015Listings(),
                massager.getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), EDITION_2015)));

        section.append(buildSection(
                "Total # of Developers with Active 2015 Listings",
                stats.getTotalDevelopersWithActive2015Listings(),
                massager.getStatisticsByStatusAndEdition(
                        stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", EDITION_2015)));

        List<CertifiedBodyStatistics> devsSuspended2015 = massager.getStatisticsByStatusAndEdition(
                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                "Suspended", EDITION_2015);
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                sumTotalDeveloperWithListings(devsSuspended2015),
                devsSuspended2015));

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

        section.append("</ul>");
        return section.toString();
    }

    private Long sumTotalDeveloperWithListings(List<CertifiedBodyStatistics> stats) {
        return stats.stream()
                .collect(Collectors.summingLong(CertifiedBodyStatistics::getTotalDevelopersWithListings));
    }
}
