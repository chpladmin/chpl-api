package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.EmailStatistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ProductStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(EmailStatistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildUniqueProductSection(stats, new StatisticsMassager(activeAcbs));
    }

    private String buildUniqueProductSection(EmailStatistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)",
                stats.getProductsForEditionAllAndAllStatuses()));section.append("<i>The sum of the ONC-ACB breakdown may not match the total, since a product may be associated to more than one ONC-ACB</i>");
                section.append("<ul>");

                section.append(buildSection(
                        "Total # of Unique Products with 2014 Listings (Regardless of Status)",
                        stats.getProductsForEdition2014WithAllStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2014WithAllStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Active 2014 Listings",
                        stats.getProductsForEdition2014WithActiveStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2014WithActiveStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings",
                        stats.getProductsForEdition2014WithSuspendedStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2014WithSuspendedStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)",
                        stats.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresAndNonCuresWithAllStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings",
                        stats.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresAndNonCuresWithActiveStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings",
                        stats.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with 2015 Listings (Regardless of Status)",
                        stats.getProductsForEdition2015NonCuresWithAllStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015NonCuresWithAllStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Active 2015 Listings",
                        stats.getProductsForEdition2015NonCuresWithActiveStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015NonCuresWithActiveStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings",
                        stats.getProductsForEdition2015NonCuresWithSuspendedStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015NonCuresWithSuspendedStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with 2015 Cures Update Listings (Regardless of Status)",
                        stats.getProductsForEdition2015CuresWithAllStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresWithAllStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Active 2015 Cures Update Listings",
                        stats.getProductsForEdition2015CuresWithActiveStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresWithActiveStatuses().getAcbStatistics())));

                section.append(buildSection(
                        "Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings",
                        stats.getProductsForEdition2015CuresWithSuspendedStatuses().getCount(),
                        massager.getStatistics(stats.getProductsForEdition2015CuresWithSuspendedStatuses().getAcbStatistics())));

                section.append("<li>Total # of Unique Products with Active Listings (Regardless of Edition) - ")
                .append(stats.getProductsForEditionAllAndActiveStatuses())
                .append("</li></ul>");

                return section.toString();
    }
}
