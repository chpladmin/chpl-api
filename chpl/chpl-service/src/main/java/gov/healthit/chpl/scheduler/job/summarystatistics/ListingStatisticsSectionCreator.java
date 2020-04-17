package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs) {
        return buildListingSection(stats, new StatisticsMassager(activeAcbs));
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildListingSection(Statistics stats, StatisticsMassager massager) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Listings (Regardless of Status or Edition)",
                stats.getTotalListings()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)",
                stats.getTotalActive2014Listings(),
                massager.getStatisticsByEdition(stats.getTotalActiveListingsByCertifiedBody(), get2014EditionAsInteger())));

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)",
                stats.getTotalActive2015Listings(),
                massager.getStatisticsByEdition(stats.getTotalActiveListingsByCertifiedBody(), get2015EditionAsInteger())));

        section.append(buildItem("Total # of 2015 Listings with Alternative Test Methods",
                stats.getTotalListingsWithAlternativeTestMethods()));

        section.append("<ul>");
        for (CertifiedBodyAltTestStatistics cbStat : massager.getStatisticsWithAltTestMethods(stats)) {
            section.append("<li>Certified by ")
                    .append(cbStat.getName())
                    .append(" - ")
                    .append(cbStat.getTotalListings())
                    .append("</li>");
        }
        section.append("</ul>");

        List<CertifiedBodyStatistics> activeListingCountWithCuresUpdatedByAcb = massager
                .getStatistics(stats.getActiveListingCountWithCuresUpdatedByAcb());
        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015-Cures Update Listings)",
                sumTotalListings(activeListingCountWithCuresUpdatedByAcb),
                activeListingCountWithCuresUpdatedByAcb));

        List<CertifiedBodyStatistics> listingCountWithCuresUpdatedAndAltTestMethodsByAcb = massager
                .getStatistics(stats.getListingCountWithCuresUpdatedAndAltTestMethodsByAcb());
        section.append(buildSection(
                "Total # of 2015-Cures Update Listings with Alternative Test Methods",
                sumTotalListings(listingCountWithCuresUpdatedAndAltTestMethodsByAcb),
                listingCountWithCuresUpdatedAndAltTestMethodsByAcb));

        section.append(buildItem("Total # of 2015-Cures Updated Listings (Regardless of Status)",
                stats.getAllListingsCountWithCuresUpdated()));
        section.append(buildItem("Total # of 2015 Listings (Regardless of Status)", stats.getTotal2015Listings()));
        section.append(buildItem("Total # of 2014 Listings (Regardless of Status)", stats.getTotal2014Listings()));
        section.append(buildItem("Total # of 2011 Listings (Regardless of Status)", stats.getTotal2011Listings()));

        section.append("</ul>");
        return section.toString();
    }
}
