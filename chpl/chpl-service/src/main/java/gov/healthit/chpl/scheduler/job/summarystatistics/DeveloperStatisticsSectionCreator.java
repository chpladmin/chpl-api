package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class DeveloperStatisticsSectionCreator extends StatisticsSectionCreator {
    private static int EDITION2015 = 2015;
    private static int EDITION2014 = 2014;

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueDeveloperSection(stats, activeAcbs);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalDevelopersWithListings();
    }

    private String buildUniqueDeveloperSection(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Unique Developers (Regardless of Edition)", stats.getTotalDevelopers()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Developers with 2014 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2014Listings(),
                getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), EDITION2014, activeAcbs)));

        section.append(buildSection(
                "Total # of Developers with Active 2014 Listings",
                stats.getTotalDevelopersWithActive2014Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", EDITION2014, activeAcbs)));

        List<CertifiedBodyStatistics> devsSuspended2014 = getStatisticsByStatusAndEdition(
                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                "Suspended", EDITION2014, activeAcbs);
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                sumTotalDeveloperWithListings(devsSuspended2014),
                devsSuspended2014));

        section.append(buildSection(
                "Total # of Developers with 2015 Listings (Regardless of Status)",
                stats.getTotalDevelopersWith2015Listings(),
                getStatisticsByEdition(stats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(), EDITION2015, activeAcbs)));

        section.append(buildSection(
                "Total # of Developers with Active 2015 Listings",
                stats.getTotalDevelopersWithActive2015Listings(),
                getStatisticsByStatusAndEdition(stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                        "Active", EDITION2015, activeAcbs)));

        List<CertifiedBodyStatistics> devsSuspended2015 = getStatisticsByStatusAndEdition(
                stats.getTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(),
                "Suspended", EDITION2015, activeAcbs);
        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                sumTotalDeveloperWithListings(devsSuspended2015),
                devsSuspended2015));

        section.append("</ul>");
        return section.toString();
    }

    private Long sumTotalDeveloperWithListings(List<CertifiedBodyStatistics> stats) {
        return stats.stream()
                .collect(Collectors.summingLong(CertifiedBodyStatistics::getTotalDevelopersWithListings));
    }
}
