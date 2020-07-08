package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildListingSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildListingSection(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Listings (Regardless of Status or Edition)",
                stats.getListingsForEditionAnyTotal()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)",
                stats.getListingsForEdition2014WithActiveAndSuspendedStatuses().getCount(),
                massager.getStatistics(stats.getListingsForEdition2014WithActiveAndSuspendedStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)",
                stats.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getCount(),
                massager.getStatistics(
                        stats.getListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of 2015 Listings with Alternative Test Methods",
                stats.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getCount(),
                massager.getStatistics(
                        stats.getListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods().getAcbStatistics())));

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Cures Update Listings)",
                stats.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getCount(),
                massager.getStatistics(stats.getListingsForEdition2015CuresWithActiveAndSuspendedStatuses().getAcbStatistics())));

        section.append(buildSection(
                "Total # of 2015 Cures Update Listings with Alternative Test Methods",
                stats.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getCount(),
                massager.getStatistics(
                        stats.getListingsForEdition2015CuresWithAllStatusesAndAltTestMethods().getAcbStatistics())));


        section.append(buildItem("Total # of 2015 Listings and 2015 Cures Update Listings (Regardless of Status)",
                stats.getListingsForEdition2015NonCuresAndCuresTotal()));

        section.append(buildItem("Total # of 2015 Listings (Regardless of Status)",
                stats.getListingsForEdition2015NonCuresTotal()));

        section.append(buildItem("Total # of 2015 Cures Update Listings (Regardless of Status)",
                stats.getListingsForEdition2015CuresTotal()));


        section.append(buildItem("Total # of 2014 Listings (Regardless of Status)", stats.getListingsForEdition2014Total()));
        section.append(buildItem("Total # of 2011 Listings (Regardless of Status)", stats.getListingsForEdition2011Total()));

        section.append("</ul>");
        return section.toString();
    }
}
