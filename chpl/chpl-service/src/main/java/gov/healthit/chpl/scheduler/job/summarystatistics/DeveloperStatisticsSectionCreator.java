package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.EmailStatistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class DeveloperStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(EmailStatistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueDeveloperSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildUniqueDeveloperSection(EmailStatistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Unique Developers (Regardless of Edition)",
                stats.getDevelopersForEditionAllAndAllStatuses()));
        section.append("<i>The sum of the ONC-ACB breakdown may not match the total, since a developer may be associated to more than one ONC-ACB</i>");
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Developers with 2014 Listings (Regardless of Status)",
                stats.getDevelopersForEdition2014WithAllStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2014WithAllStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Active 2014 Listings",
                stats.getDevelopersForEdition2014WithActiveStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2014WithActiveStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                stats.getDevelopersForEdition2014WithSuspendedStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2014WithSuspendedStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)",
                stats.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings",
                stats.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getCount(),
                massager.getStatistics(
                        stats.getDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings",
                stats.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount(),
                massager.getStatistics(
                        stats.getDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with 2015 Listings (Regardless of Status)",
                stats.getDevelopersForEdition2015NonCuresWithAllStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015NonCuresWithAllStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Active 2015 Listings",
                stats.getDevelopersForEdition2015NonCuresWithActiveStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015NonCuresWithActiveStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                stats.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics())));


        section.append(buildSection(
                "Total # of Developers with 2015 Cures Update Listings (Regardless of Status)",
                stats.getDevelopersForEdition2015CuresWithAllStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015CuresWithAllStatuses().getAcbStatistics())));


        section.append(buildSection(
                "Total # of Developers with Active 2015 Cures Update Listings",
                stats.getDevelopersForEdition2015CuresWithActiveStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015CuresWithActiveStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings",
                stats.getDevelopersForEdition2015CuresWithSuspendedStatuses().getCount(),
                massager.getStatistics(stats.getDevelopersForEdition2015CuresWithSuspendedStatuses().getAcbStatistics())));

        section.append("</ul>");

        return section.toString();
    }
}
