package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public class ListingStatisticsSectionCreator extends StatisticsSectionCreator {

    public String build(Statistics stats, List<CertificationBodyDTO> activeAcbs, FF4j ff4j) {
        return buildListingSection(stats, new StatisticsMassager(activeAcbs), ff4j);
    }

    @Override
    public Long getStatistic(CertifiedBodyStatistics stat) {
        return stat.getTotalListings();
    }

    private String buildListingSection(Statistics stats, StatisticsMassager massager, FF4j ff4j) {
        StringBuilder section = new StringBuilder();

        section.append(buildHeader("Total # of Listings (Regardless of Status or Edition)",
                stats.getTotalListings()));
        section.append("<ul>");

        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)",
                stats.getTotalActive2014Listings(),
                massager.getStatisticsByEdition(stats.getTotalActiveListingsByCertifiedBody(), get2014EditionAsInteger())));

        List<CertifiedBodyStatistics> activeListingCountFor2015ByAcb = massager
                .getStatistics(stats.getActiveListingCountFor2015ByAcb());
        section.append(buildSection(
                "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)",
                sumTotalListings(activeListingCountFor2015ByAcb),
                activeListingCountFor2015ByAcb));

        List<CertifiedBodyStatistics> listingCountWith2015AndAltTestMethodsByAcb = massager
                .getStatistics(stats.getTotalListingsWithCertifiedBodyAndAlternativeTestMethods());
        section.append(buildSection(
                "Total # of 2015 Listings with Alternative Test Methods",
                sumTotalListings(listingCountWith2015AndAltTestMethodsByAcb),
                listingCountWith2015AndAltTestMethodsByAcb));

        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE)) {
            List<CertifiedBodyStatistics> activeListingCountWithCuresUpdatedByAcb = massager
                    .getStatistics(stats.getActiveListingCountWithCuresUpdatedByAcb());
            section.append(buildSection(
                    "Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Cures Update Listings)",
                    sumTotalListings(activeListingCountWithCuresUpdatedByAcb),
                    activeListingCountWithCuresUpdatedByAcb));

            List<CertifiedBodyStatistics> listingCountWithCuresUpdatedAndAltTestMethodsByAcb = massager
                    .getStatistics(stats.getListingCountWithCuresUpdatedAndAltTestMethodsByAcb());
            section.append(buildSection(
                    "Total # of 2015 Cures Update Listings with Alternative Test Methods",
                    sumTotalListings(listingCountWithCuresUpdatedAndAltTestMethodsByAcb),
                    listingCountWithCuresUpdatedAndAltTestMethodsByAcb));

            section.append(buildItem("Total # of 2015 Listings and Cures Update Listings (Regardless of Status)",
                    stats.getTotal2015Listings()));

            section.append(buildItem("Total # of 2015 Listings (Regardless of Status)",
                    stats.getAllListingsCountWithoutCuresUpdated()));

            section.append(buildItem("Total # of 2015 Cures Update Listings (Regardless of Status)",
                    stats.getAllListingsCountWithCuresUpdated()));
        } else {
            section.append(buildItem("Total # of 2015 Listings (Regardless of Status)", stats.getTotal2015Listings()));
        }


        section.append(buildItem("Total # of 2014 Listings (Regardless of Status)", stats.getTotal2014Listings()));
        section.append(buildItem("Total # of 2011 Listings (Regardless of Status)", stats.getTotal2011Listings()));

        section.append("</ul>");
        return section.toString();
    }
}
